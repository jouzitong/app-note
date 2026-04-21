import { createHttp, required, withQuery } from "@/api/http-client";

const server = "/api/v1/articles/domain";
const $http = createHttp(server);

const api = {
  getArticle: function (articleId) {
    required(articleId, "articleId");
    return $http.get(`/${encodeURIComponent(articleId)}`);
  },

  getArticleByNoteNode: function (noteNodeId) {
    const numericNodeId = Number(noteNodeId);
    if (!Number.isInteger(numericNodeId) || numericNodeId <= 0) {
      throw new Error("noteNodeId is required");
    }
    return $http.get(`/note-node/${encodeURIComponent(numericNodeId)}`);
  },

  saveArticle: function (article) {
    return $http.post("", article || {});
  },

  updateArticleFavorite: function (articleId, favorite) {
    required(articleId, "articleId");
    return $http.post(
      withQuery(`/${encodeURIComponent(articleId)}/favorite`, {
        favorite: Boolean(favorite),
      })
    );
  },

  updateArticlePlaybackRate: function (articleId, playbackRate) {
    required(articleId, "articleId");
    return $http.post(
      withQuery(`/${encodeURIComponent(articleId)}/playback-rate`, {
        playbackRate,
      })
    );
  },

  updateArticlePosition: function (articleId, paragraphIndex) {
    required(articleId, "articleId");
    return $http.post(
      withQuery(`/${encodeURIComponent(articleId)}/position`, {
        paragraphIndex,
      })
    );
  },
};

export default api;
