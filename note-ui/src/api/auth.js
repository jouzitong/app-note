import { requestJson, unwrapResponse } from "@/utils/http";

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
    json: payload,
    auth: false,
    clearAuthOn401: false,
    skipAuthRefresh: true,
  });
  return unwrapResponse(response);
}

export async function refreshToken() {
  const response = await requestJson("/auth/refresh", {
    method: "POST",
    skipAuthRefresh: true,
    clearAuthOn401: false,
    redirectOn401: false,
  });
  return unwrapResponse(response);
}

export async function getCurrentUser() {
  const response = await requestJson("/auth/me");
  return unwrapResponse(response);
}

export async function logout() {
  await requestJson("/auth/logout", {
    method: "POST",
    skipAuthRefresh: true,
  });
}
