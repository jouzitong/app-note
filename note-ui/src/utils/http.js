import { clearAuth, getAuthToken } from "@/utils/auth";

export class HttpError extends Error {
  constructor(status, statusText, bodyText) {
    super(`HTTP ${status} ${statusText}: ${bodyText || "request failed"}`);
    this.name = "HttpError";
    this.status = status;
    this.statusText = statusText;
    this.bodyText = bodyText || "";
  }
}

function resolvePath(path) {
  if (!path) {
    return "";
  }
  if (/^https?:\/\//i.test(path)) {
    return path;
  }
  if (path.startsWith("/")) {
    return path;
  }
  return `/${path}`;
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

export async function requestJson(path, options = {}) {
  const headers = {
    Accept: "application/json",
    ...(options.body ? { "Content-Type": "application/json" } : {}),
    ...(options.headers || {}),
  };

  if (options.auth !== false) {
    const token = getAuthToken();
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
  }

  const response = await fetch(resolvePath(path), {
    ...options,
    headers,
  });

  if (!response.ok) {
    const errorText = await response.text();
    if (response.status === 401 && options.clearAuthOn401 !== false) {
      clearAuth();
      if (options.redirectOn401 !== false) {
        redirectToLoginOnUnauthorized();
      }
    }
    throw new HttpError(response.status, response.statusText, errorText);
  }

  const contentType = response.headers.get("content-type") || "";
  if (!contentType.includes("application/json")) {
    return null;
  }
  return response.json();
}
