import { clearAuth, getAuthToken, saveAuthToken } from "@/utils/auth";

export class HttpError extends Error {
  constructor({
    status,
    statusText,
    url,
    method,
    bodyText,
    bodyJson,
    cause,
  } = {}) {
    super(
      `HTTP ${status ?? 0} ${statusText || ""}${method ? ` ${method}` : ""}${
        url ? ` ${url}` : ""
      }: ${bodyText || "request failed"}`
    );
    this.name = "HttpError";
    this.status = status ?? 0;
    this.statusText = statusText || "";
    this.url = url || "";
    this.method = method || "";
    this.bodyText = bodyText || "";
    this.bodyJson = bodyJson ?? null;
    this.cause = cause;
  }
}

const API_BASE_URL = (process.env.VUE_APP_API_BASE_URL || "").replace(
  /\/+$/,
  ""
);

function isAbsoluteUrl(path) {
  return /^https?:\/\//i.test(path);
}

const hooks = {
  onRequest: null,
  onResponse: null,
  onError: null,
  onUnauthorized: null,
};

export function configureHttpHooks(nextHooks = {}) {
  hooks.onRequest = nextHooks.onRequest || null;
  hooks.onResponse = nextHooks.onResponse || null;
  hooks.onError = nextHooks.onError || null;
  hooks.onUnauthorized = nextHooks.onUnauthorized || null;
}

function resolvePath(path) {
  if (!path) {
    return "";
  }
  if (isAbsoluteUrl(path)) {
    return path;
  }
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  if (!API_BASE_URL) {
    return normalizedPath;
  }
  if (
    !isAbsoluteUrl(API_BASE_URL) &&
    (normalizedPath === API_BASE_URL ||
      normalizedPath.startsWith(`${API_BASE_URL}/`))
  ) {
    return normalizedPath;
  }
  return `${API_BASE_URL}${normalizedPath}`;
}

function redirectToLoginOnUnauthorized() {
  if (typeof window === "undefined") {
    return;
  }
  const currentPath = window.location.pathname || "";
  if (currentPath === "/login" || currentPath.endsWith("/login")) {
    return;
  }
  const redirect = `${window.location.pathname || "/"}${
    window.location.search || ""
  }${window.location.hash || ""}`;
  const nextUrl = `/login?redirect=${encodeURIComponent(redirect)}`;
  window.location.assign(nextUrl);
}

function unwrapEnvelope(data) {
  if (data == null) {
    return data;
  }
  if (Object.prototype.hasOwnProperty.call(data, "data")) {
    return data.data;
  }
  if (Object.prototype.hasOwnProperty.call(data, "result")) {
    return data.result;
  }
  return data;
}

function extractToken(payload) {
  if (!payload || typeof payload !== "object") {
    return "";
  }
  return (
    payload.token ||
    payload.accessToken ||
    payload.access_token ||
    payload.jwt ||
    ""
  );
}

let refreshPromise = null;
async function refreshAuthTokenOnce() {
  if (refreshPromise) {
    return refreshPromise;
  }
  refreshPromise = (async () => {
    const token = getAuthToken();
    try {
      const headers = {
        Accept: "application/json",
      };
      if (token) {
        headers.Authorization = `Bearer ${token}`;
      }

      const res = await fetch(resolvePath("/auth/refresh"), {
        method: "POST",
        headers,
      });

      if (!res.ok) {
        return "";
      }
      const contentType = res.headers.get("content-type") || "";
      if (!contentType.includes("application/json")) {
        return "";
      }
      const body = await res.json();
      const unwrapped = unwrapEnvelope(body);
      const nextToken = extractToken(unwrapped);
      if (nextToken) {
        saveAuthToken(nextToken);
      }
      return nextToken;
    } catch (error) {
      return "";
    } finally {
      refreshPromise = null;
    }
  })();
  return refreshPromise;
}

function shouldSetJsonContentType(body) {
  if (!body) {
    return false;
  }
  if (typeof body === "string") {
    return true;
  }
  if (typeof FormData !== "undefined" && body instanceof FormData) {
    return false;
  }
  if (typeof Blob !== "undefined" && body instanceof Blob) {
    return false;
  }
  if (typeof ArrayBuffer !== "undefined" && body instanceof ArrayBuffer) {
    return false;
  }
  return false;
}

export async function requestJson(path, options = {}) {
  const method = (options.method || "GET").toUpperCase();
  const url = resolvePath(path);

  let body = options.body;
  if (body === undefined && options.json !== undefined) {
    body = JSON.stringify(options.json);
  }

  const headers = {
    Accept: "application/json",
    ...(options.headers || {}),
  };

  if (!headers["Content-Type"] && shouldSetJsonContentType(body)) {
    headers["Content-Type"] = "application/json";
  }

  if (options.auth !== false) {
    const token = getAuthToken();
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
  }

  let response;
  try {
    if (typeof hooks.onRequest === "function") {
      try {
        hooks.onRequest({ url, method, headers, body, options });
      } catch (error) {
        // ignore hook error
      }
    }
    response = await fetch(url, {
      ...options,
      body,
      method,
      headers,
    });
  } catch (error) {
    const httpError = new HttpError({
      status: 0,
      statusText: "NETWORK_ERROR",
      url,
      method,
      cause: error,
    });
    if (typeof hooks.onError === "function") {
      try {
        hooks.onError(httpError, { url, method, options });
      } catch (hookError) {
        // ignore
      }
    }
    throw httpError;
  }

  if (!response.ok) {
    const contentType = response.headers.get("content-type") || "";
    let errorText = "";
    let errorJson = null;
    try {
      if (contentType.includes("application/json")) {
        errorJson = await response.json();
        const unwrapped = unwrapEnvelope(errorJson);
        errorText =
          (unwrapped && (unwrapped.message || unwrapped.msg)) ||
          JSON.stringify(unwrapped || errorJson);
      } else {
        errorText = await response.text();
      }
    } catch (error) {
      errorText = errorText || "request failed";
    }

    if (
      response.status === 401 &&
      options.auth !== false &&
      options.skipAuthRefresh !== true &&
      options.__retried !== true
    ) {
      const nextToken = await refreshAuthTokenOnce();
      if (nextToken) {
        return requestJson(path, {
          ...options,
          __retried: true,
        });
      }
    }

    if (response.status === 401 && options.clearAuthOn401 !== false) {
      clearAuth();
      if (typeof hooks.onUnauthorized === "function") {
        try {
          hooks.onUnauthorized({ url, method, options });
        } catch (error) {
          // ignore
        }
      }
      if (options.redirectOn401 !== false) {
        redirectToLoginOnUnauthorized();
      }
    }

    const httpError = new HttpError({
      status: response.status,
      statusText: response.statusText,
      url,
      method,
      bodyText: errorText,
      bodyJson: errorJson,
    });
    if (typeof hooks.onError === "function") {
      try {
        hooks.onError(httpError, { url, method, options });
      } catch (hookError) {
        // ignore
      }
    }
    throw httpError;
  }

  const contentType = response.headers.get("content-type") || "";
  if (!contentType.includes("application/json")) {
    if (typeof hooks.onResponse === "function") {
      try {
        hooks.onResponse({ url, method, status: response.status, options });
      } catch (error) {
        // ignore
      }
    }
    return null;
  }
  const json = await response.json();
  if (typeof hooks.onResponse === "function") {
    try {
      hooks.onResponse({ url, method, status: response.status, options });
    } catch (error) {
      // ignore
    }
  }
  return json;
}

export function unwrapResponse(data) {
  return unwrapEnvelope(data);
}
