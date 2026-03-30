/**
 * @typedef {Object} NoteTagDTO
 * @property {number|null} id
 * @property {string} name
 */

/**
 * @typedef {Object} NoteNodeMetaDTO
 * @property {Object|null} node
 * @property {string} icon
 * @property {NoteTagDTO[]} tags
 * @property {string} subject
 */

/**
 * @typedef {Object} NoteNodeDTO
 * @property {number|null} id
 * @property {number|null} parentId
 * @property {string} title
 * @property {string} noteType
 * @property {number} sort
 * @property {string} content
 * @property {NoteNodeMetaDTO} meta
 */

/**
 * 与后端 NoteNodeDTO 对齐的默认结构。
 * @returns {NoteNodeDTO}
 */
export function createDefaultNoteNode() {
  return {
    id: null,
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

/**
 * 页面展示用的示例数据（结构与 NoteNodeDTO 对齐）。
 * @returns {NoteNodeDTO}
 */
export function createMockNoteNode() {
  return {
    id: 20260327001,
    parentId: null,
    title: "これは私の本です",
    noteType: "VOCAB_NOTE",
    sort: 10,
    content: JSON.stringify({
      paragraphs: [
        "这是一个 noteNode 的展示区域，用来承载当前节点的正文内容。",
        "你可以在这里展示文本、说明、例句、结构化内容，或者后续扩展为富文本渲染区域。",
        "如果你的 noteNode 是“特定业务模板笔记”，这里还可以根据 noteType 动态切换不同内容区域。例如：单词卡片、知识点、题目、复习记录、案例说明等。",
      ],
      bullets: [
        "支持展示节点标题",
        "支持展示类型、学科、排序等基础属性",
        "支持后续扩展标签、状态、模板字段",
      ],
    }),
    meta: {
      node: null,
      icon: "📘",
      tags: [
        { id: 1, name: "N5" },
        { id: 2, name: "基础句型" },
        { id: 3, name: "指示代词" },
      ],
      subject: "日语",
    },
  };
}
