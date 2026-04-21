import { createHttp, withQuery } from "@/api/http-client";

const server = "/api/v1/noteNodes/domain";
const appServer = "/api/v1/app/noteNodes";
const NOTE_TAG_BIZ_TYPE = "NOTE";

const $http = createHttp(server);
const $appHttp = createHttp(appServer);

function pickList(body) {
  if (Array.isArray(body)) {
    return body;
  }
  if (Array.isArray(body?.records)) {
    return body.records;
  }
  if (Array.isArray(body?.content)) {
    return body.content;
  }
  if (Array.isArray(body?.items)) {
    return body.items;
  }
  return [];
}

function normalizeNoteNodeAddPayload(payload) {
  const source = payload && typeof payload === "object" ? payload : {};
  const rawNoteNode = source.noteNode || source;
  const noteNode =
    rawNoteNode && rawNoteNode.noteNode ? rawNoteNode.noteNode : rawNoteNode;

  return {
    noteNode: noteNode || null,
    pathIds: source.pathIds ?? null,
    content: source.content ?? null,
  };
}

const api = {
  listNoteNodes: async function (params = {}) {
    const body = await $http.get("", { params });
    return pickList(body);
  },

  listNoteNodesByParentId: function (parentId, options = {}) {
    const params = {
      page: options.page || 1,
      size: options.size || 100,
      sorts: options.sorts || "sort:ASC",
    };
    if (parentId !== null && parentId !== undefined && parentId !== "") {
      params.queries = `parentId:${parentId}:1`;
    }
    return api.listNoteNodes(params);
  },

  getNoteNodeById: async function (id) {
    const body = await $http.get(`/${id}`);
    return {
      noteNode: body?.noteNode || null,
      paths: Array.isArray(body?.paths) ? body.paths : [],
      childNoteNodes: Array.isArray(body?.childNoteNodes)
        ? body.childNoteNodes
        : [],
      content: body?.content ?? null,
    };
  },

  getNoteNodeContentByType: async function (id, params = {}) {
    const body = await $appHttp.get(withQuery(`/${id}/content`, params));
    return {
      noteNode: body?.noteNode || null,
      paths: Array.isArray(body?.paths) ? body.paths : [],
      childNoteNodes: Array.isArray(body?.childNoteNodes)
        ? body.childNoteNodes
        : [],
      noteType: body?.noteType ?? null,
      contentType: body?.contentType ?? null,
      content: body?.content ?? null,
      ext: body?.ext ?? {},
    };
  },

  createNoteNode: function (payload) {
    const body = normalizeNoteNodeAddPayload(payload);
    return $http.post("", body);
  },

  updateNoteNode: function (id, payload) {
    const body = normalizeNoteNodeAddPayload(payload);
    return $http.put(`/${id}`, body);
  },

  deleteNoteNode: function (id) {
    return $http.delete(`/${id}`);
  },

  searchParentNoteNodes: async function (params = {}) {
    const body = await $http.get("/parents/search", { params });
    return pickList(body);
  },

  searchNoteTags: async function (params = {}) {
    const body = await $http.get("/tags/search", {
      params: {
        bizType: NOTE_TAG_BIZ_TYPE,
        ...params,
      },
    });
    return pickList(body);
  },

  createNoteTag: function (payload) {
    const body = {
      bizType: NOTE_TAG_BIZ_TYPE,
      ...(payload || {}),
    };
    return $http.post("/tags", body);
  },
};

export default api;
