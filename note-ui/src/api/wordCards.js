const API_BASE_URL = process.env.VUE_APP_API_BASE_URL || "/api";
const normalizedBase = API_BASE_URL.replace(/\/+$/, "");
const BASE_PATH = normalizedBase.endsWith("/api")
  ? `${normalizedBase}/v1/wordCards/domain`
  : `${normalizedBase}/api/v1/wordCards/domain`;

async function request(path, options = {}) {
  const response = await fetch(path, {
    headers: {
      Accept: "application/json",
      ...(options.body ? { "Content-Type": "application/json" } : {}),
      ...(options.headers || {}),
    },
    ...options,
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(
      `HTTP ${response.status} ${response.statusText}: ${
        errorText || "request failed"
      }`
    );
  }

  const contentType = response.headers.get("content-type") || "";
  if (!contentType.includes("application/json")) {
    return null;
  }

  return response.json();
}

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

export async function getWordCardByNoteAndIndex(noteId, index) {
  const response = await request(`${BASE_PATH}/${noteId}/${index}`);
  return unwrap(response);
}

export async function getWordCardPage({
  noteId,
  page = 1,
  size = 10,
  userId,
} = {}) {
  const params = new URLSearchParams();
  if (noteId !== undefined && noteId !== null && `${noteId}` !== "") {
    params.append("noteId", `${noteId}`);
  }
  if (page !== undefined && page !== null) {
    params.append("page", `${page}`);
  }
  if (size !== undefined && size !== null) {
    params.append("size", `${size}`);
  }
  if (userId !== undefined && userId !== null && `${userId}` !== "") {
    params.append("userId", `${userId}`);
  }

  const queryString = params.toString();
  const response = await request(
    `${BASE_PATH}${queryString ? `?${queryString}` : ""}`
  );
  return unwrap(response);
}
