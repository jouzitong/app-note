import { requestJson, unwrapResponse } from "@/utils/http";

const BASE_PATH = "/api/v1/articles/domain";

export async function getArticle(articleId) {
  if (!articleId) {
    throw new Error("articleId is required");
  }
  const response = await requestJson(
    `${BASE_PATH}/${encodeURIComponent(articleId)}`
  );
  return unwrapResponse(response);
}

export async function getArticleByNoteNode(noteNodeId) {
  const numericNodeId = Number(noteNodeId);
  if (!Number.isInteger(numericNodeId) || numericNodeId <= 0) {
    throw new Error("noteNodeId is required");
  }
  const response = await requestJson(
    `${BASE_PATH}/note-node/${encodeURIComponent(numericNodeId)}`
  );
  return unwrapResponse(response);
}

export async function saveArticle(article) {
  await requestJson(BASE_PATH, {
    method: "POST",
    json: article || {},
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
  return unwrapResponse(response);
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
  return unwrapResponse(response);
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
  return unwrapResponse(response);
}
