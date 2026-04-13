import { requestJson, unwrapResponse } from "@/utils/http";

const BASE_PATH = "/api/v1/wordCards/domain";

export async function getWordCardByNoteAndIndex(noteId, index) {
  const response = await requestJson(`${BASE_PATH}/${noteId}/${index}`);
  return unwrapResponse(response);
}

export async function confirmWordCardDone(cardId, userId) {
  if (cardId === undefined || cardId === null || `${cardId}` === "") {
    throw new Error("cardId is required");
  }
  const params = new URLSearchParams();
  if (userId !== undefined && userId !== null && `${userId}` !== "") {
    params.append("userId", `${userId}`);
  }
  const queryString = params.toString();
  const response = await requestJson(
    `${BASE_PATH}/${encodeURIComponent(cardId)}/confirm${
      queryString ? `?${queryString}` : ""
    }`,
    {
      method: "POST",
    }
  );
  return unwrapResponse(response);
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
  const response = await requestJson(
    `${BASE_PATH}${queryString ? `?${queryString}` : ""}`
  );
  const records = unwrapResponse(response);
  return {
    records: Array.isArray(records) ? records : [],
    pageInfo: response?.pageInfo || null,
    raw: response,
  };
}
