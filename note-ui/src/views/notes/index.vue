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
      </div>

      <div class="content-layout">
        <div class="main-content">
          <div class="card">
            <h2 class="section-title">子节点</h2>
            <ul class="tree-list">
              <li
                v-for="(item, index) in childNodeTitles"
                :key="item"
                class="tree-item"
                :class="{ active: index === 0 }"
              >
                {{ index + 1 }}. {{ item }}
              </li>
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
import {
  createDefaultNoteNode,
  createMockNoteNode,
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
      childNodeTitles: [
        "これは私の本です",
        "それは先生の本です",
        "あれは日本語の本です",
        "疑问词练习",
      ],
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
      try {
        const parsed = JSON.parse(this.noteNode.content || "{}");
        return {
          paragraphs: parsed.paragraphs || [],
          bullets: parsed.bullets || [],
        };
      } catch (error) {
        return {
          paragraphs: [],
          bullets: [],
        };
      }
    },
  },
  created() {
    this.noteNode = createMockNoteNode();
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
}

.tree-item:last-child,
.info-item:last-child {
  border-bottom: none;
}

.tree-item.active {
  color: #2563eb;
  font-weight: 600;
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
