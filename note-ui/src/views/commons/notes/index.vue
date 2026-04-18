<template>
  <div class="note-page">
    <div class="page">
      <div class="note-header">
        <div class="breadcrumb">{{ breadcrumbText }}</div>

        <div class="note-title-row">
          <h1 class="note-title">{{ noteNode.title }}</h1>
          <div class="note-actions">
            <button
              class="icon-btn"
              title="返回上一级"
              aria-label="返回上一级"
              @click="goToParent"
            >
              ↩
            </button>
            <button
              class="icon-btn"
              title="编辑"
              aria-label="编辑"
              @click="openEditPage"
            >
              ✎
            </button>
          </div>
        </div>

        <div class="meta-row">
          <div class="meta-icons">
            <span class="meta-chip"
              ><span class="chip-icon">T</span>{{ noteNode.noteType }}</span
            >
          </div>
          <span
            v-for="tag in noteTags"
            :key="`${tag.id || tag.label}-${tag.className}`"
            class="app-tag"
            :class="tag.className"
          >
            {{ tag.label }}
          </span>
        </div>
        <div v-if="loading" class="note-status">正在加载节点数据...</div>
        <div v-else-if="errorMessage" class="note-status error">
          {{ errorMessage }}
        </div>
      </div>

      <div class="content-layout">
        <div class="main-content">
          <div class="card">
            <div class="section-head">
              <h2 class="section-title">内容</h2>
              <span class="meta-chip">{{ contentTypeLabel }}</span>
            </div>

            <template v-if="contentType === 'WORD_CARD_PAGE'">
              <ul v-if="wordCardRecords.length" class="info-list">
                <li
                  v-for="item in wordCardRecords"
                  :key="item.id || item.word?.text"
                  class="info-item"
                >
                  {{ item.word?.text || item.id || "未命名单词卡" }}
                </li>
              </ul>
              <div v-else class="note-status">该节点暂无单词卡内容。</div>
              <button class="minor-btn content-enter-btn" @click="openWordCard">
                进入词卡页
              </button>
            </template>

            <template v-else-if="contentType === 'ARTICLE_DETAIL'">
              <div v-if="articleDetail" class="note-content">
                <h3>{{ articleDetail.title || "未命名文章" }}</h3>
                <p>段落数：{{ articleParagraphCount }}</p>
              </div>
              <div v-else class="note-status">该节点暂无文章内容。</div>
              <button class="minor-btn content-enter-btn" @click="openArticle">
                进入文章页
              </button>
            </template>

            <template v-else-if="contentType === 'QUESTION_PAGE'">
              <ul v-if="questionRecords.length" class="info-list">
                <li
                  v-for="item in questionRecords"
                  :key="item.id || item.questionCode"
                  class="info-item"
                >
                  {{ item.title || item.questionCode || "未命名题目" }}
                </li>
              </ul>
              <div v-else class="note-status">该节点暂无题目内容。</div>
              <button class="minor-btn content-enter-btn" @click="openPractice">
                进入练习页
              </button>
            </template>

            <template v-else>
              <div v-if="!contentText" class="note-status">
                该节点暂无内容。
              </div>
              <pre v-else class="content-json">{{ contentText }}</pre>
            </template>
          </div>

          <div class="card">
            <div class="section-head">
              <h2 class="section-title">子节点</h2>
              <button class="minor-btn" type="button" @click="handleAddChild">
                新增
              </button>
            </div>
            <ul class="tree-list">
              <li
                v-for="(item, index) in childNodes"
                :key="item.id"
                class="tree-item"
                @click="openChildNode(item)"
              >
                <span class="tree-item-title"
                  >{{ index + 1 }}. {{ item.title }}</span
                >
                <span class="tree-item-actions">
                  <button
                    class="item-action-btn"
                    type="button"
                    title="编辑"
                    aria-label="编辑"
                    @click.stop="handleEditChild(item)"
                  >
                    <i class="el-icon-edit item-action-icon" aria-hidden="true"
                      >✎</i
                    >
                  </button>
                  <button
                    class="item-action-btn danger"
                    type="button"
                    title="删除"
                    aria-label="删除"
                    @click.stop="handleDeleteChild(item)"
                  >
                    <i
                      class="el-icon-delete item-action-icon"
                      aria-hidden="true"
                      >🗑</i
                    >
                  </button>
                </span>
              </li>
              <li v-if="!childNodes.length" class="tree-item">暂无子节点</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getNoteNodeById, getNoteNodeContentByType } from "@/api/noteNodes";
import { saveLastLanguageJpNoteId } from "@/utils/languageJpNav";
import {
  createDefaultNoteNode,
  normalizeNoteNode,
} from "@/model/note/noteNode";

export default {
  name: "NoteIndexView",
  data() {
    return {
      noteNode: createDefaultNoteNode(),
      childNodes: [],
      paths: [],
      noteContent: null,
      contentType: "NOTE_NODE_CONTENT",
      contentExt: {},
      loading: false,
      errorMessage: "",
    };
  },
  computed: {
    noteTags() {
      const tags = this.noteNode?.meta?.tags;
      if (!Array.isArray(tags)) {
        return [];
      }
      return tags
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
            label,
            className: item?.className || "app-tag--info",
          };
        })
        .filter(Boolean);
    },
    breadcrumbText() {
      return this.paths
        .map((item) => item.title)
        .filter(Boolean)
        .join(" / ");
    },
    contentTypeLabel() {
      const labels = {
        NOTE_NODE_CONTENT: "通用内容",
        WORD_CARD_PAGE: "单词卡",
        ARTICLE_DETAIL: "文章",
        QUESTION_PAGE: "题目",
        UNSUPPORTED: "未支持",
      };
      return labels[this.contentType] || this.contentType || "未知";
    },
    wordCardRecords() {
      const content = this.noteContent;
      if (Array.isArray(content)) {
        return content;
      }
      if (Array.isArray(content?.records)) {
        return content.records;
      }
      return [];
    },
    questionRecords() {
      const content = this.noteContent;
      if (Array.isArray(content)) {
        return content;
      }
      if (Array.isArray(content?.records)) {
        return content.records;
      }
      return [];
    },
    articleDetail() {
      const content = this.noteContent;
      if (!content || Array.isArray(content) || typeof content !== "object") {
        return null;
      }
      if (
        !("id" in content) &&
        !("title" in content) &&
        !("paragraphs" in content)
      ) {
        return null;
      }
      return content;
    },
    articleParagraphCount() {
      const paragraphs = this.articleDetail?.paragraphs;
      return Array.isArray(paragraphs) ? paragraphs.length : 0;
    },
    contentText() {
      const content = this.noteContent;
      if (content === null || content === undefined || content === "") {
        return "";
      }
      if (typeof content === "string") {
        return content;
      }
      try {
        return JSON.stringify(content, null, 2);
      } catch (error) {
        return String(content);
      }
    },
  },
  async created() {
    await this.loadPageData();
  },
  watch: {
    "$route.fullPath"() {
      this.loadPageData();
    },
  },
  methods: {
    resolveNoteId() {
      const routeId = this.$route.params.id || this.$route.query.id;
      if (routeId !== undefined && routeId !== null && routeId !== "") {
        const parsed = Number(routeId);
        if (!Number.isNaN(parsed)) {
          return parsed;
        }
      }
      return null;
    },
    async loadPageData() {
      this.loading = true;
      this.errorMessage = "";
      try {
        const noteId = this.resolveNoteId();
        if (noteId === null) {
          this.noteNode = createDefaultNoteNode();
          this.paths = [];
          this.noteContent = null;
          this.contentType = "NOTE_NODE_CONTENT";
          this.contentExt = {};
          this.childNodes = [];
          this.errorMessage =
            "请通过 /language-jp/materials/{id} 访问指定节点。";
          return;
        }

        const noteVO = await this.loadNoteByTypedApi(noteId);
        const note = noteVO?.noteNode || noteVO;
        saveLastLanguageJpNoteId(noteId);

        this.noteNode = normalizeNoteNode(note || {});
        this.paths = Array.isArray(noteVO?.paths) ? noteVO.paths : [];
        this.noteContent = noteVO?.content ?? this.noteNode.content;
        this.contentType = noteVO?.contentType || "NOTE_NODE_CONTENT";
        this.contentExt =
          noteVO?.ext && typeof noteVO.ext === "object" ? noteVO.ext : {};
        this.childNodes = (noteVO?.childNoteNodes || [])
          .map((item) => ({
            id: item.id,
            title: item.title,
            sort: 0,
            noteType: item.noteType,
          }))
          .filter((item) => item.id && item.title);
      } catch (error) {
        this.noteNode = createDefaultNoteNode();
        this.paths = [];
        this.noteContent = null;
        this.contentType = "NOTE_NODE_CONTENT";
        this.contentExt = {};
        this.childNodes = [];
        this.errorMessage = `接口请求失败：${error.message}`;
      } finally {
        this.loading = false;
      }
    },
    async loadNoteByTypedApi(noteId) {
      try {
        return await getNoteNodeContentByType(noteId, {
          page: 1,
          size: 10,
          includeChildren: true,
        });
      } catch (error) {
        const fallback = await getNoteNodeById(noteId);
        return {
          ...fallback,
          contentType: "NOTE_NODE_CONTENT",
          ext: {
            fallback: true,
            fallbackReason: error?.message || "typed api failed",
          },
        };
      }
    },
    resolveNoteTypeCode(noteType) {
      if (noteType === null || noteType === undefined || noteType === "") {
        return null;
      }
      const raw = String(noteType).trim();
      const num = Number(raw);
      if (Number.isFinite(num)) {
        return num;
      }
      const upper = raw.toUpperCase();
      if (upper === "WORD_CARD" || upper === "WORD") {
        return 2;
      }
      if (upper === "ARTICLE") {
        return 4;
      }
      if (
        upper === "PRACTICE" ||
        upper === "QUESTIONS" ||
        upper === "QUESTION"
      ) {
        return 100;
      }
      return null;
    },
    openChildNode(childNode) {
      if (!childNode || !childNode.id) {
        return;
      }
      const typeCode = this.resolveNoteTypeCode(childNode.noteType);
      if (typeCode === 2) {
        this.openWordCardByNode(childNode.id);
        return;
      }
      if (typeCode === 4) {
        this.openArticleByNode(childNode.id);
        return;
      }
      if (typeCode === 100) {
        this.openPracticeByNode(childNode.id);
        return;
      }
      this.$router.push({
        name: "language-jp-materials",
        params: { id: String(childNode.id) },
      });
    },
    getParentIdForContentNav() {
      return this.noteNode && this.noteNode.id
        ? String(this.noteNode.id)
        : this.$route.params.id || this.$route.query.id;
    },
    openWordCardByNode(nodeId) {
      const parentId = this.getParentIdForContentNav();
      if (!parentId || !nodeId) {
        this.errorMessage = "缺少父节点ID或节点ID，无法进入单词卡页面";
        return;
      }
      this.$router.push({
        name: "language-jp-word-card",
        params: { parentId: String(parentId) },
        query: {
          nodeId: String(nodeId),
          pageIndex: "1",
          wordIndex: "0",
        },
      });
    },
    openArticleByNode(nodeId) {
      const parentId = this.getParentIdForContentNav();
      if (!parentId || !nodeId) {
        this.errorMessage = "缺少父节点ID或节点ID，无法进入文章页面";
        return;
      }
      this.$router.push({
        name: "language-jp-article",
        params: { parentId: String(parentId) },
        query: { nodeId: String(nodeId) },
      });
    },
    openPracticeByNode(nodeId) {
      const parentId = this.getParentIdForContentNav();
      if (!parentId || !nodeId) {
        this.errorMessage = "缺少父节点ID或节点ID，无法进入练习页面";
        return;
      }
      this.$router.push({
        name: "language-jp-practice",
        params: { parentId: String(parentId) },
        query: { nodeId: String(nodeId) },
      });
    },
    openWordCard() {
      this.openWordCardByNode(this.noteNode?.id);
    },
    openArticle() {
      this.openArticleByNode(this.noteNode?.id);
    },
    openPractice() {
      this.openPracticeByNode(this.noteNode?.id);
    },
    handleAddChild() {
      const parentId =
        this.noteNode && this.noteNode.id
          ? this.noteNode.id
          : this.resolveNoteId();
      this.$router.push({
        name: "language-jp-materials-new",
        query: parentId ? { parentId: String(parentId) } : {},
      });
    },
    handleEditChild(childNode) {
      if (!childNode || !childNode.id) {
        this.errorMessage = "缺少子节点ID，无法进入编辑页。";
        return;
      }
      this.$router.push({
        name: "language-jp-materials-edit",
        params: { id: String(childNode.id) },
      });
    },
    handleDeleteChild() {
      window.alert("子节点删除功能暂未实现。");
    },
    goToParent() {
      if (this.paths.length >= 2) {
        const parent = this.paths[this.paths.length - 2];
        if (parent && parent.id) {
          this.$router.push({
            name: "language-jp-materials",
            params: { id: String(parent.id) },
          });
          return;
        }
      }
      this.$router.back();
    },
    openEditPage() {
      if (!this.noteNode.id) {
        return;
      }
      this.$router.push({
        name: "language-jp-materials-edit",
        params: { id: String(this.noteNode.id) },
      });
    },
  },
};
</script>

<style scoped>
* {
  box-sizing: border-box;
}

.note-page {
  min-height: 100vh;
  min-height: 100dvh;
  background: #f5f7fb;
  color: #1f2937;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC",
    "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
  padding-bottom: calc(56px + env(safe-area-inset-bottom));
}

.page {
  max-width: 960px;
  margin: 0 auto;
  padding: 20px 16px 40px;
}

.note-header {
  background: #ffffff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 6px 20px rgba(15, 23, 42, 0.06);
  margin-bottom: 16px;
}

.breadcrumb {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 12px;
  text-align: left;
}

.note-title-row {
  position: relative;
  min-height: 38px;
  margin-bottom: 12px;
}

.note-title {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.3;
  margin: 0;
  color: #111827;
  text-align: center;
  padding-right: 96px;
  padding-left: 96px;
}

.note-actions {
  display: flex;
  gap: 10px;
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
}

.icon-btn {
  width: 38px;
  height: 38px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #f8fafc;
  color: #374151;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  cursor: pointer;
  transition: 0.2s ease;
}

.icon-btn:hover {
  background: #eef2ff;
  color: #1d4ed8;
  border-color: #c7d2fe;
}

.meta-icons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 4px;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 6px;
}

.meta-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  border-radius: 999px;
  font-size: 11px;
  color: #4b5563;
  background: #f3f4f6;
  border: 1px solid #e5e7eb;
  white-space: nowrap;
}

.chip-icon {
  font-size: 12px;
}

.content-layout {
  display: grid;
  grid-template-columns: 1fr 260px;
  gap: 16px;
  align-items: start;
}

.card {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 6px 20px rgba(15, 23, 42, 0.06);
  padding: 20px;
}

.main-content .card + .card {
  margin-top: 16px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 14px;
  color: #111827;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.section-head .section-title {
  margin-bottom: 0;
}

.minor-btn {
  height: 30px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: #ffffff;
  color: #374151;
  padding: 0 10px;
  font-size: 13px;
  cursor: pointer;
}

.minor-btn:hover {
  border-color: #9ca3af;
}

.note-content {
  line-height: 1.9;
  font-size: 15px;
  color: #374151;
}

.note-content h3 {
  margin-top: 24px;
  margin-bottom: 10px;
  font-size: 17px;
  color: #111827;
}

.note-content p {
  margin: 0 0 14px;
}

.note-content ul {
  margin: 0 0 14px 20px;
  padding: 0;
}

.content-json {
  margin: 0;
  padding: 12px;
  border-radius: 10px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  color: #374151;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.content-enter-btn {
  margin-top: 12px;
}

.aside-block + .aside-block {
  margin-top: 16px;
}

.tree-list,
.info-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.tree-item,
.info-item {
  padding: 10px 0;
  border-bottom: 1px solid #eef2f7;
  font-size: 14px;
  color: #4b5563;
}

.tree-item {
  text-align: left;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.tree-item:hover {
  color: #2563eb;
}

.tree-item-title {
  min-width: 0;
}

.tree-item-actions {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.item-action-btn {
  width: 28px;
  height: 28px;
  border: 1px solid #d1d5db;
  background: #fff;
  color: #374151;
  border-radius: 8px;
  line-height: 1;
  padding: 0;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.item-action-btn:hover {
  border-color: #9ca3af;
}

.item-action-icon {
  font-size: 14px;
  font-style: normal;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.item-action-btn.danger {
  color: #b91c1c;
  border-color: #fecaca;
}

.tree-item:last-child,
.info-item:last-child {
  border-bottom: none;
}

.tree-item.active {
  color: #2563eb;
  font-weight: 600;
}

.note-status {
  margin-top: 12px;
  font-size: 13px;
  color: #6b7280;
}

.note-status.error {
  color: #b91c1c;
}

@media (max-width: 900px) {
  .content-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 600px) {
  .page {
    padding: 14px 12px 28px;
  }

  .note-title {
    font-size: 22px;
  }

  .card,
  .note-header {
    padding: 16px;
    border-radius: 14px;
  }

  .note-actions {
    position: static;
    transform: none;
    gap: 8px;
    justify-content: flex-end;
    margin-top: 8px;
  }

  .note-title {
    padding-left: 0;
    padding-right: 0;
  }
}
</style>
