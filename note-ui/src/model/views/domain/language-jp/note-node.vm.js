export function createDefaultNoteTagVm() {
  return {
    id: null,
    bizType: "NOTE",
    label: "",
    className: "app-tag--info",
  };
}

export function createDefaultNoteNodeVm() {
  return {
    id: null,
    noteKey: "",
    parentId: null,
    title: "",
    noteType: "",
    sort: 0,
    content: "",
    meta: {
      node: null,
      icon: "",
      tags: [],
      subject: "",
    },
  };
}

export function createDefaultNoteNodeDetailVm() {
  return {
    noteNode: createDefaultNoteNodeVm(),
    paths: [],
    childNodes: [],
    content: null,
  };
}
