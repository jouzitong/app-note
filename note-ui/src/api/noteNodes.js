import { requestJson } from "@/utils/http";

const API_BASE_URL = process.env.VUE_APP_API_BASE_URL || "/api";
const normalizedBase = API_BASE_URL.replace(/\/+$/, "");
const BASE_PATH = normalizedBase.endsWith("/api")
  ? `${normalizedBase}/v1/noteNodes/domain`
  : `${normalizedBase}/api/v1/noteNodes/domain`;

function toQueryString(params = {}) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === "") {
      return;
    }
    query.append(key, value);
  });
  const q = query.toString();
  return q ? `?${q}` : "";
}

function unwrap(data) {
  if (data == null) {
    return data;
  }
  if (Array.isArray(data)) {
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

export async function listNoteNodes(params = {}) {
  const res = await requestJson(`${BASE_PATH}${toQueryString(params)}`);
  const body = unwrap(res);
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

export async function listNoteNodesByParentId(parentId, options = {}) {
  const params = {
    page: options.page || 1,
    size: options.size || 100,
    sorts: options.sorts || "sort:ASC",
  };
  if (parentId !== null && parentId !== undefined && parentId !== "") {
    params.queries = `parentId:${parentId}:1`;
  }
  return listNoteNodes(params);
}

export async function getNoteNodeById(id) {
  const res = await requestJson(`${BASE_PATH}/${id}`);
  const body = unwrap(res);
  return {
    noteNode: body?.noteNode || null,
    paths: Array.isArray(body?.paths) ? body.paths : [],
    childNoteNodes: Array.isArray(body?.childNoteNodes)
      ? body.childNoteNodes
      : [],
    content: body?.content ?? null,
  };
}

export async function createNoteNode(payload) {
  return requestJson(BASE_PATH, {
    method: "POST",
    body: JSON.stringify({
      noteNode: payload.noteNode || payload,
      pathIds: payload.pathIds || null,
      content: payload.content || null,
    }),
  });
}

export async function updateNoteNode(id, payload) {
  return requestJson(`${BASE_PATH}/${id}`, {
    method: "PUT",
    body: JSON.stringify({
      noteNode: payload.noteNode || payload,
      pathIds: payload.pathIds || null,
      content: payload.content || null,
    }),
  });
}

export async function deleteNoteNode(id) {
  return requestJson(`${BASE_PATH}/${id}`, {
    method: "DELETE",
  });
}
