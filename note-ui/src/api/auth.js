import { requestJson } from "@/utils/http";

function unwrap(data) {
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

export async function login(username, password, tenantId = "") {
  const payload = {
    username: (username || "").trim(),
    password: password || "",
  };
  if (tenantId) {
    payload.tenantId = tenantId;
  }
  const response = await requestJson("/auth/login", {
    method: "POST",
    body: JSON.stringify(payload),
    auth: false,
    clearAuthOn401: false,
  });
  return unwrap(response);
}

export async function refreshToken() {
  const response = await requestJson("/auth/refresh", {
    method: "POST",
  });
  return unwrap(response);
}

export async function getCurrentUser() {
  const response = await requestJson("/auth/me");
  return unwrap(response);
}

export async function logout() {
  await requestJson("/auth/logout", {
    method: "POST",
  });
}
