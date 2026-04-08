import { requestJson } from "@/utils/http";

const API_BASE_URL = process.env.VUE_APP_API_BASE_URL || "/api";
const normalizedBase = API_BASE_URL.replace(/\/+$/, "");
const BASE_PATH = normalizedBase.endsWith("/api")
  ? `${normalizedBase}/v1/articles/domain`
  : `${normalizedBase}/api/v1/articles/domain`;

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

export async function getArticle(articleId) {
  if (!articleId) {
    throw new Error("articleId is required");
  }
  const response = await requestJson(
    `${BASE_PATH}/${encodeURIComponent(articleId)}`
  );
  return unwrap(response);
}

export async function getArticleByNoteNode(noteNodeId) {
  const numericNodeId = Number(noteNodeId);
  if (!Number.isInteger(numericNodeId) || numericNodeId <= 0) {
    throw new Error("noteNodeId is required");
  }
  const response = await requestJson(
    `${BASE_PATH}/note-node/${encodeURIComponent(numericNodeId)}`
  );
  return unwrap(response);
}

export async function saveArticle(article) {
  await requestJson(BASE_PATH, {
    method: "POST",
    body: JSON.stringify(article || {}),
  });
}

export async function updateArticleFavorite(articleId, favorite) {
  const params = new URLSearchParams();
  params.append("favorite", `${Boolean(favorite)}`);
  const response = await requestJson(
    `${BASE_PATH}/${encodeURIComponent(
      articleId
    )}/favorite?${params.toString()}`,
    { method: "POST" }
  );
  return unwrap(response);
}

export async function updateArticlePlaybackRate(articleId, playbackRate) {
  const params = new URLSearchParams();
  params.append("playbackRate", `${playbackRate}`);
  const response = await requestJson(
    `${BASE_PATH}/${encodeURIComponent(
      articleId
    )}/playback-rate?${params.toString()}`,
    { method: "POST" }
  );
  return unwrap(response);
}

export async function updateArticlePosition(articleId, paragraphIndex) {
  const params = new URLSearchParams();
  params.append("paragraphIndex", `${paragraphIndex}`);
  const response = await requestJson(
    `${BASE_PATH}/${encodeURIComponent(
      articleId
    )}/position?${params.toString()}`,
    { method: "POST" }
  );
  return unwrap(response);
}
