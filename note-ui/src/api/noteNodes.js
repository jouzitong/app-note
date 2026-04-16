import { requestJson, unwrapResponse } from "@/utils/http";

const BASE_PATH = "/api/v1/noteNodes/domain";
const NOTE_TAG_BIZ_TYPE = "NOTE";

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
  const body = unwrapResponse(data);
  if (Array.isArray(body)) {
    return body;
  }
  return body;
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
  const body = normalizeNoteNodeAddPayload(payload);
  return requestJson(BASE_PATH, {
    method: "POST",
    json: body,
  });
}

export async function updateNoteNode(id, payload) {
  const body = normalizeNoteNodeAddPayload(payload);
  return requestJson(`${BASE_PATH}/${id}`, {
    method: "PUT",
    json: body,
  });
}

export async function deleteNoteNode(id) {
  return requestJson(`${BASE_PATH}/${id}`, {
    method: "DELETE",
  });
}

export async function searchParentNoteNodes(params = {}) {
  const res = await requestJson(
    `${BASE_PATH}/parents/search${toQueryString(params)}`
  );
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

export async function searchNoteTags(params = {}) {
  const query = {
    bizType: NOTE_TAG_BIZ_TYPE,
    ...params,
  };
  const res = await requestJson(
    `${BASE_PATH}/tags/search${toQueryString(query)}`
  );
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

export async function createNoteTag(payload) {
  const body = {
    bizType: NOTE_TAG_BIZ_TYPE,
    ...(payload || {}),
  };
  const res = await requestJson(`${BASE_PATH}/tags`, {
    method: "POST",
    json: body,
  });
  return unwrap(res);
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
