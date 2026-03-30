<template>
  <div class="note-page">
    <div class="page">
      <div class="note-header">
        <div class="breadcrumb">首页 / 笔记 / 日语学习 / N5核心词汇</div>

        <div class="note-title-row">
          <h1 class="note-title">{{ noteNode.title }}</h1>
          <div class="note-actions">
            <button class="icon-btn" title="返回上一级" aria-label="返回上一级">
              ↩
            </button>
            <button class="icon-btn" title="编辑" aria-label="编辑">✎</button>
          </div>
        </div>

        <div class="meta-icons">
          <span class="meta-chip"
            ><span class="chip-icon">#</span>{{ noteIdLabel }}</span
          >
          <span class="meta-chip"
            ><span class="chip-icon">T</span>{{ noteNode.noteType }}</span
          >
          <span class="meta-chip"
            ><span class="chip-icon">S</span>{{ noteNode.meta.subject }}</span
          >
          <span class="meta-chip"
            ><span class="chip-icon">↕</span>{{ noteNode.sort }}</span
          >
        </div>
        <div v-if="loading" class="note-status">正在加载节点数据...</div>
        <div v-else-if="errorMessage" class="note-status error">
          {{ errorMessage }}
        </div>
      </div>

      <div class="content-layout">
        <div class="main-content">
          <div class="card">
            <h2 class="section-title">子节点</h2>
            <ul class="tree-list">
              <li
                v-for="item in childNodes"
                :key="item.id"
                class="tree-item"
                @click="openChildNode(item)"
              >
                {{ item.sort }}. {{ item.title }}
              </li>
              <li v-if="!childNodes.length" class="tree-item">暂无子节点</li>
            </ul>
          </div>

          <NoteMarkdownContent :content-data="contentData" />
        </div>

        <div class="side-content">
          <div class="card aside-block">
            <h2 class="section-title">扩展字段</h2>
            <ul class="info-list">
              <li class="info-item">模板类型：句型笔记</li>
              <li class="info-item">标签：{{ tagText }}</li>
              <li class="info-item">难度：入门</li>
              <li class="info-item">复习状态：待复习</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getNoteNodeById, listNoteNodesByParentId } from "@/api/noteNodes";
import {
  createDefaultNoteNode,
  createMockNoteNode,
  normalizeNoteNode,
} from "@/model/note/noteNode";
import NoteMarkdownContent from "@/components/notes/NoteMarkdownContent.vue";

export default {
  name: "NoteIndexView",
  components: {
    NoteMarkdownContent,
  },
  data() {
    return {
      noteNode: createDefaultNoteNode(),
      childNodes: [],
      loading: false,
      errorMessage: "",
    };
  },
  computed: {
    noteIdLabel() {
      if (!this.noteNode.id) {
        return "--";
      }
      return `NN-${this.noteNode.id}`;
    },
    tagText() {
      return (this.noteNode.meta.tags || [])
        .map((item) => item.name)
        .join(" / ");
    },
    contentData() {
      const fallback = createMockNoteNode();
      const fallbackContent = JSON.parse(fallback.content);

      const rawContent = this.noteNode.content;
      if (rawContent && typeof rawContent === "object") {
        return {
          paragraphs: rawContent.paragraphs || fallbackContent.paragraphs,
          bullets: rawContent.bullets || fallbackContent.bullets,
        };
      }

      try {
        const parsed = JSON.parse(rawContent || "{}");
        return {
          paragraphs: parsed.paragraphs || fallbackContent.paragraphs,
          bullets: parsed.bullets || fallbackContent.bullets,
        };
      } catch (error) {
        return fallbackContent;
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
          const rootList = await listNoteNodesByParentId(null, {
            page: 1,
            size: 100,
            sorts: "sort:ASC",
          });
          this.childNodes = rootList
            .map((item) => normalizeNoteNode(item))
            .sort((a, b) => (a.sort || 0) - (b.sort || 0))
            .map((item) => ({
              id: item.id,
              title: item.title,
              sort: item.sort || 0,
            }))
            .filter((item) => item.id && item.title);
          return;
        }

        const noteResponse = await getNoteNodeById(noteId);
        const note =
          noteResponse && noteResponse.noteNode
            ? noteResponse.noteNode
            : noteResponse;

        this.noteNode = normalizeNoteNode(note || {});
        const parentIdForQuery = this.noteNode.id ?? noteId;
        const list = await listNoteNodesByParentId(parentIdForQuery, {
          page: 1,
          size: 100,
          sorts: "sort:ASC",
        });
        this.childNodes = list
          .map((item) => normalizeNoteNode(item))
          .sort((a, b) => (a.sort || 0) - (b.sort || 0))
          .map((item) => ({
            id: item.id,
            title: item.title,
            sort: item.sort || 0,
          }))
          .filter((item) => item.id && item.title);
      } catch (error) {
        const mock = createMockNoteNode();
        this.noteNode = normalizeNoteNode(mock);
        this.childNodes = [
          { id: 1, title: "これは私の本です", sort: 1 },
          { id: 2, title: "それは先生の本です", sort: 2 },
          { id: 3, title: "あれは日本語の本です", sort: 3 },
          { id: 4, title: "疑问词练习", sort: 4 },
        ];
        this.errorMessage = `接口请求失败，已使用示例数据：${error.message}`;
      } finally {
        this.loading = false;
      }
    },
    openChildNode(childNode) {
      if (!childNode || !childNode.id) {
        return;
      }
      this.$router.push({
        name: "note",
        params: { id: String(childNode.id) },
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
  background: #f5f7fb;
  color: #1f2937;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC",
    "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
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
}

.note-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.note-title {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.3;
  margin: 0;
  color: #111827;
}

.note-actions {
  display: flex;
  gap: 10px;
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
}

.tree-item:hover {
  color: #2563eb;
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
    gap: 8px;
  }
}
</style>
