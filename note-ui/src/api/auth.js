import { createHttp } from "@/api/http-client";

const $http = createHttp("");

const api = {
  login: function (username, password, tenantId = "") {
    const payload = {
      username: (username || "").trim(),
      password: password || "",
    };
    if (tenantId) {
      payload.tenantId = tenantId;
    }

    return $http.post("/auth/login", payload, {
      auth: false,
      silentError: true,
      clearAuthOn401: false,
      skipAuthRefresh: true,
    });
  },

  refreshToken: function () {
    return $http.post("/auth/refresh", undefined, {
      skipAuthRefresh: true,
      clearAuthOn401: false,
      redirectOn401: false,
    });
  },

  getCurrentUser: function () {
    return $http.get("/auth/me");
  },

  logout: function () {
    return $http.post("/auth/logout", undefined, {
      skipAuthRefresh: true,
    });
  },
};

export default api;
