import {
  createNoteNode,
  createNoteTag,
  getNoteNodeById,
  searchNoteTags,
  searchParentNoteNodes,
  updateNoteNode,
} from "@/api/noteNodes";
import {
  mapNoteNodeDetailDtoToVm,
  mapParentNodeToOptionVm,
  mapTagToOptionVm,
} from "@/mappers/domain/language-jp/note-node.mapper";

export async function fetchNoteNodeDetail(noteNodeId) {
  const detailDto = await getNoteNodeById(noteNodeId);
  return mapNoteNodeDetailDtoToVm(detailDto || {});
}

export async function fetchParentNoteNodeDisplay(parentId) {
  const detail = await fetchNoteNodeDetail(parentId);
  const pathText = (detail.paths || [])
    .map((item) => item.title)
    .filter(Boolean)
    .join(" / ");
  return pathText || detail.noteNode?.title || `节点 ${parentId}`;
}

export async function searchParentNoteNodeOptions({
  keyword = "",
  excludeId = null,
  limit = 20,
} = {}) {
  const list = await searchParentNoteNodes({
    keyword: String(keyword || "").trim(),
    excludeId,
    limit,
  });
  return (Array.isArray(list) ? list : [])
    .map((item) => mapParentNodeToOptionVm(item))
    .filter(Boolean);
}

export async function searchNoteTagOptions({ keyword = "", limit = 20 } = {}) {
  const list = await searchNoteTags({
    keyword: String(keyword || "").trim(),
    limit,
  });
  return (Array.isArray(list) ? list : [])
    .map((item) => mapTagToOptionVm(item))
    .filter(Boolean);
}

export async function createNoteTagOption(payload = {}) {
  const created = await createNoteTag(payload);
  return (
    mapTagToOptionVm(created || {}) || {
      id: null,
      bizType: "NOTE",
      label: String(payload?.label || "").trim(),
      className: payload?.className || "app-tag--info",
    }
  );
}

export async function createNoteNodeByDraft(payload) {
  return createNoteNode(payload);
}

export async function updateNoteNodeByDraft(noteNodeId, payload) {
  return updateNoteNode(noteNodeId, payload);
}
