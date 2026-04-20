export function createDefaultNoteTagDto() {
  return {
    id: null,
    bizType: "NOTE",
    label: "",
    className: "",
  };
}

export function createDefaultNoteNodeMetaDto() {
  return {
    node: null,
    icon: "",
    tags: [],
    subject: "",
  };
}

export function createDefaultNoteNodeDto() {
  return {
    id: null,
    noteKey: "",
    parentId: null,
    title: "",
    noteType: "",
    sort: 0,
    content: "",
    meta: createDefaultNoteNodeMetaDto(),
  };
}

export function createDefaultNoteNodeDetailDto() {
  return {
    noteNode: createDefaultNoteNodeDto(),
    paths: [],
    childNoteNodes: [],
    content: null,
  };
}
