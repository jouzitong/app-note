import { requestJson } from "@/utils/http";

const API_BASE_URL = process.env.VUE_APP_API_BASE_URL || "/api";
const normalizedBase = API_BASE_URL.replace(/\/+$/, "");
const BASE_PATH = normalizedBase.endsWith("/api")
  ? `${normalizedBase}/v1/practices`
  : `${normalizedBase}/api/v1/practices`;

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

export async function createPracticeSession({
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
  const response = await requestJson(`${BASE_PATH}/sessions`, {
    method: "POST",
    body: JSON.stringify(payload),
  });
  return unwrap(response);
}

export async function getPracticeItem(sessionId, index, userId) {
  if (sessionId === undefined || sessionId === null || `${sessionId}` === "") {
    throw new Error("sessionId is required");
  }
  if (index === undefined || index === null || Number(index) < 0) {
    throw new Error("index is required");
  }
  const params = new URLSearchParams();
  if (userId !== undefined && userId !== null && `${userId}` !== "") {
    params.append("userId", `${userId}`);
  }
  const queryString = params.toString();
  const response = await requestJson(
    `${BASE_PATH}/sessions/${encodeURIComponent(
      sessionId
    )}/items/${encodeURIComponent(index)}${
      queryString ? `?${queryString}` : ""
    }`
  );
  return unwrap(response);
}

export async function submitPracticeAnswer(sessionId, payload) {
  if (sessionId === undefined || sessionId === null || `${sessionId}` === "") {
    throw new Error("sessionId is required");
  }
  const response = await requestJson(
    `${BASE_PATH}/sessions/${encodeURIComponent(sessionId)}/submit`,
    {
      method: "POST",
      body: JSON.stringify(payload || {}),
    }
  );
  return unwrap(response);
}

export async function getPracticeStats(noteNodeId, userId) {
  const params = new URLSearchParams();
  params.append("noteNodeId", `${noteNodeId}`);
  if (userId !== undefined && userId !== null && `${userId}` !== "") {
    params.append("userId", `${userId}`);
  }
  const response = await requestJson(`${BASE_PATH}/stats?${params.toString()}`);
  return unwrap(response);
}

export async function getWrongQuestions({
  noteNodeId,
  page = 1,
  size = 20,
  userId,
} = {}) {
  const params = new URLSearchParams();
  if (
    noteNodeId !== undefined &&
    noteNodeId !== null &&
    `${noteNodeId}` !== ""
  ) {
    params.append("noteNodeId", `${noteNodeId}`);
  }
  params.append("page", `${page}`);
  params.append("size", `${size}`);
  if (userId !== undefined && userId !== null && `${userId}` !== "") {
    params.append("userId", `${userId}`);
  }
  const response = await requestJson(
    `${BASE_PATH}/wrong-questions?${params.toString()}`
  );
  return unwrap(response);
}
