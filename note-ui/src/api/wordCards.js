import { createHttp, required } from "@/api/http-client";
import { unwrapResponse } from "@/utils/http";

const server = "/api/v1/wordCards/domain";
const $http = createHttp(server);

const api = {
  getWordCardByNoteAndIndex: function (noteId, index) {
    return $http.get(
      `/${encodeURIComponent(noteId)}/${encodeURIComponent(index)}`
    );
  },

  confirmWordCardDone: function (cardId, userId) {
    required(cardId, "cardId");
    return $http.post(`/${encodeURIComponent(cardId)}/confirm`, undefined, {
      params: { userId },
    });
  },

  getWordCardPage: async function ({
    noteId,
    page = 1,
    size = 10,
    userId,
  } = {}) {
    const response = await $http.get("", {
      params: {
        noteId,
        page,
        size,
        userId,
      },
      raw: true,
    });

    const records = unwrapResponse(response);
    return {
      records: Array.isArray(records) ? records : [],
      pageInfo: response?.pageInfo || null,
      raw: response,
    };
  },
};

export default api;
