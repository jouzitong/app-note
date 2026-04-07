const API_BASE_URL = process.env.VUE_APP_API_BASE_URL || "/api";
const normalizedBase = API_BASE_URL.replace(/\/+$/, "");
const BASE_PATH = normalizedBase.endsWith("/api")
  ? `${normalizedBase}/v1/articles/domain`
  : `${normalizedBase}/api/v1/articles/domain`;

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

export async function getArticle(articleId, userId) {
  if (!articleId) {
    throw new Error("articleId is required");
  }
  const params = new URLSearchParams();
  if (userId !== undefined && userId !== null && `${userId}` !== "") {
    params.append("userId", `${userId}`);
  }
  const query = params.toString();
  const response = await request(
    `${BASE_PATH}/${encodeURIComponent(articleId)}${query ? `?${query}` : ""}`
  );
  return unwrap(response);
}

export async function getArticleByNoteNode(noteNodeId, userId) {
  const numericNodeId = Number(noteNodeId);
  if (!Number.isInteger(numericNodeId) || numericNodeId <= 0) {
    throw new Error("noteNodeId is required");
  }
  const params = new URLSearchParams();
  if (userId !== undefined && userId !== null && `${userId}` !== "") {
    params.append("userId", `${userId}`);
  }
  const query = params.toString();
  const response = await request(
    `${BASE_PATH}/note-node/${encodeURIComponent(numericNodeId)}${
      query ? `?${query}` : ""
    }`
  );
  return unwrap(response);
}

export async function saveArticle(article) {
  await request(BASE_PATH, {
    method: "POST",
    body: JSON.stringify(article || {}),
  });
}

export async function updateArticleFavorite(articleId, favorite, userId) {
  const params = new URLSearchParams();
  params.append("favorite", `${Boolean(favorite)}`);
  if (userId !== undefined && userId !== null && `${userId}` !== "") {
    params.append("userId", `${userId}`);
  }
  const response = await request(
    `${BASE_PATH}/${encodeURIComponent(
      articleId
    )}/favorite?${params.toString()}`,
    { method: "POST" }
  );
  return unwrap(response);
}

export async function updateArticlePlaybackRate(
  articleId,
  playbackRate,
  userId
) {
  const params = new URLSearchParams();
  params.append("playbackRate", `${playbackRate}`);
  if (userId !== undefined && userId !== null && `${userId}` !== "") {
    params.append("userId", `${userId}`);
  }
  const response = await request(
    `${BASE_PATH}/${encodeURIComponent(
      articleId
    )}/playback-rate?${params.toString()}`,
    { method: "POST" }
  );
  return unwrap(response);
}

export async function updateArticlePosition(articleId, paragraphIndex, userId) {
  const params = new URLSearchParams();
  params.append("paragraphIndex", `${paragraphIndex}`);
  if (userId !== undefined && userId !== null && `${userId}` !== "") {
    params.append("userId", `${userId}`);
  }
  const response = await request(
    `${BASE_PATH}/${encodeURIComponent(
      articleId
    )}/position?${params.toString()}`,
    { method: "POST" }
  );
  return unwrap(response);
}
