import { createHttp, required } from "@/api/http-client";

const API_BASE_URL = process.env.VUE_APP_API_BASE_URL || "/api";
const normalizedBase = API_BASE_URL.replace(/\/+$/, "");
const server = normalizedBase.endsWith("/api")
  ? `${normalizedBase}/v1/practices`
  : `${normalizedBase}/api/v1/practices`;
const $http = createHttp(server);

const api = {
  createPracticeSession: function ({
    noteNodeId,
    mode = "SEQUENTIAL",
    size = 20,
    userId,
  } = {}) {
    const payload = {
      noteNodeId,
      mode,
      size,
    };
    if (userId !== undefined && userId !== null && `${userId}` !== "") {
      payload.userId = userId;
    }

    return $http.post("/sessions", payload);
  },

  getPracticeItem: function (sessionId, index, userId) {
    required(sessionId, "sessionId");
    if (index === undefined || index === null || Number(index) < 0) {
      throw new Error("index is required");
    }

    return $http.get(
      `/sessions/${encodeURIComponent(sessionId)}/items/${encodeURIComponent(
        index
      )}`,
      {
        params: { userId },
      }
    );
  },

  submitPracticeAnswer: function (sessionId, payload) {
    required(sessionId, "sessionId");
    return $http.post(
      `/sessions/${encodeURIComponent(sessionId)}/submit`,
      payload || {}
    );
  },

  getPracticeStats: function (noteNodeId, userId) {
    return $http.get("/stats", {
      params: {
        noteNodeId,
        userId,
      },
    });
  },

  getWrongQuestions: function ({
    noteNodeId,
    page = 1,
    size = 20,
    userId,
  } = {}) {
    return $http.get("/wrong-questions", {
      params: {
        noteNodeId,
        page,
        size,
        userId,
      },
    });
  },
};

export default api;
