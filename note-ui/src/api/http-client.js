import { requestJson, unwrapResponse } from "@/utils/http";

function trimSlash(value = "") {
  return String(value || "").replace(/\/+$/, "");
}

function isNilOrEmpty(value) {
  return value === undefined || value === null || value === "";
}

export function toQueryString(params = {}) {
  const query = new URLSearchParams();
  Object.entries(params || {}).forEach(([key, value]) => {
    if (isNilOrEmpty(value)) {
      return;
    }
    query.append(key, value);
  });
  const queryString = query.toString();
  return queryString ? `?${queryString}` : "";
}

export function withQuery(path, params = {}) {
  return `${path}${toQueryString(params)}`;
}

export function required(value, name) {
  if (isNilOrEmpty(value)) {
    throw new Error(`${name} is required`);
  }
}

export function createHttp(server = "") {
  const base = trimSlash(server);

  function resolve(path = "") {
    if (!path) {
      return base || "";
    }
    if (/^https?:\/\//i.test(path)) {
      return path;
    }
    if (!base) {
      return path;
    }
    if (path.startsWith("/")) {
      return `${base}${path}`;
    }
    return `${base}/${path}`;
  }

  async function request(method, path, data, options = {}) {
    const { params, raw = false, ...requestOptions } = options || {};

    const url = withQuery(resolve(path), params);
    const response = await requestJson(url, {
      method,
      ...(data !== undefined ? { json: data } : {}),
      ...requestOptions,
    });
    return raw ? response : unwrapResponse(response);
  }

  return {
    get(path = "", options = {}) {
      return request("GET", path, undefined, options);
    },
    post(path = "", data, options = {}) {
      return request("POST", path, data, options);
    },
    put(path = "", data, options = {}) {
      return request("PUT", path, data, options);
    },
    patch(path = "", data, options = {}) {
      return request("PATCH", path, data, options);
    },
    delete(path = "", options = {}) {
      return request("DELETE", path, undefined, options);
    },
  };
}
