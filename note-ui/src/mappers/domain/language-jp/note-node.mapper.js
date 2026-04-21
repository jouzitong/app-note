import {
  createDefaultNoteNodeDetailDto,
  createDefaultNoteNodeDto,
} from "@/model/api/note/note-node.dto";
import {
  createDefaultNoteNodeDetailVm,
  createDefaultNoteNodeVm,
} from "@/model/views/domain/language-jp/note-node.vm";

const FALLBACK_TAG_CLASS_NAME = "app-tag--info";

function normalizeTags(sourceTags) {
  if (!Array.isArray(sourceTags)) {
    return [];
  }
  return sourceTags
    .map((item) => {
      const label =
        typeof item?.label === "string"
          ? item.label.trim()
          : typeof item?.name === "string"
          ? item.name.trim()
          : "";
      if (!label) {
        return null;
      }
      return {
        id: item?.id || null,
        bizType: item?.bizType || "NOTE",
        label,
        className: item?.className || FALLBACK_TAG_CLASS_NAME,
      };
    })
    .filter(Boolean);
}

export function mapNoteNodeDtoToVm(source = {}) {
  const fallback = createDefaultNoteNodeVm();
  const dto = {
    ...createDefaultNoteNodeDto(),
    ...(source || {}),
  };
  const meta = dto.meta && typeof dto.meta === "object" ? dto.meta : {};
  const tags = normalizeTags(meta.tags);

  return {
    ...fallback,
    ...dto,
    meta: {
      ...fallback.meta,
      ...meta,
      tags,
    },
  };
}

export function mapNoteNodeDetailDtoToVm(source = {}) {
  const fallback = createDefaultNoteNodeDetailVm();
  const dto = {
    ...createDefaultNoteNodeDetailDto(),
    ...(source || {}),
  };
  const noteNode = mapNoteNodeDtoToVm(dto.noteNode || dto);
  const paths = Array.isArray(dto.paths) ? dto.paths : [];
  const childNoteNodes = Array.isArray(dto.childNoteNodes)
    ? dto.childNoteNodes
    : [];

  return {
    ...fallback,
    noteNode,
    paths: paths
      .map((item) => ({
        id: item?.id || null,
        title: item?.title || "",
      }))
      .filter((item) => item.title),
    childNodes: childNoteNodes
      .map((item) => ({
        id: item?.id || null,
        title: item?.title || "",
        sort: Number.isFinite(Number(item?.sort)) ? Number(item.sort) : 0,
        noteType: item?.noteType || "",
        tags: normalizeTags(item?.meta?.tags || item?.tags),
      }))
      .filter((item) => item.id && item.title),
    content: dto.content ?? noteNode.content ?? null,
  };
}

export function mapParentNodeToOptionVm(source = {}) {
  const id = Number(source?.id);
  if (!Number.isInteger(id) || id <= 0) {
    return null;
  }
  return {
    id,
    title: source?.title || `节点 ${id}`,
  };
}

export function mapTagToOptionVm(source = {}) {
  const label = String(source?.label || "").trim();
  if (!label) {
    return null;
  }
  return {
    id: source?.id || null,
    bizType: source?.bizType || "NOTE",
    label,
    className: source?.className || FALLBACK_TAG_CLASS_NAME,
  };
}
