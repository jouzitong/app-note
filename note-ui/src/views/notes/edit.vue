<template>
  <div class="note-edit-page">
    <div class="page">
      <div class="edit-header card">
        <div class="header-top">
          <button
            class="icon-btn"
            title="返回详情"
            aria-label="返回详情"
            @click="goBack"
          >
            ↩
          </button>
          <h1 class="title">编辑笔记节点</h1>
          <button class="save-btn" type="button" @click="handleSave">
            保存（暂未接入）
          </button>
        </div>
        <div v-if="loading" class="status">正在加载节点数据...</div>
        <div v-else-if="errorMessage" class="status error">
          {{ errorMessage }}
        </div>
      </div>

      <div class="card">
        <div class="form-grid">
          <label class="field">
            <span class="label">父节点ID</span>
            <input v-model="form.parentId" type="number" disabled />
          </label>
          <label class="field">
            <span class="label">标题</span>
            <input v-model="form.title" type="text" />
          </label>
          <label class="field">
            <span class="label">类型 noteType</span>
            <select v-model="form.noteType">
              <option disabled value="">请选择类型</option>
              <option
                v-for="item in noteTypeOptions"
                :key="item.value"
                :value="item.value"
              >
                {{ item.label }}
              </option>
            </select>
          </label>
          <label class="field">
            <span class="label">排序 sort</span>
            <input v-model.number="form.sort" type="number" />
          </label>
          <label class="field">
            <span class="label">图标 icon</span>
            <input v-model="form.icon" type="text" />
          </label>
          <label class="field">
            <span class="label">标签（逗号分隔）</span>
            <input v-model="form.tagsText" type="text" />
          </label>
        </div>

        <label class="field block">
          <span class="label">内容 content（JSON）</span>
          <textarea v-model="form.contentText" rows="12"></textarea>
        </label>
      </div>
    </div>
  </div>
</template>

<script>
import { getNoteNodeById } from "@/api/noteNodes";
import {
  createDefaultNoteNode,
  normalizeNoteNode,
} from "@/model/note/noteNode";

const DEFAULT_NOTE_TYPE_OPTIONS = [
  { value: 1, label: "MARKDOWN (1) - Markdown" },
  { value: 2, label: "WORD_CARD (2) - Word Card" },
  { value: 3, label: "SENTENCE (3) - Sentence" },
  { value: 4, label: "QUESTIONS (4) - Questions" },
];

function normalizeEnumOption(item) {
  if (item == null || typeof item !== "object") {
    return null;
  }

  const value = [item.code, item.value, item.id, item.key].find(
    (it) => it !== undefined && it !== null && it !== ""
  );
  if (value === undefined || value === null || value === "") {
    return null;
  }

  const codeText = item.code ?? value;
  const nameText = item.name || item.label || item.text || item.key || "";
  const descText = item.desc || item.description || "";
  const label = descText
    ? `${nameText} (${codeText}) - ${descText}`
    : `${nameText} (${codeText})`;

  return {
    value: Number.isNaN(Number(value)) ? value : Number(value),
    label,
  };
}

function resolveNoteTypeOptions(enums) {
  if (!enums || typeof enums !== "object") {
    return DEFAULT_NOTE_TYPE_OPTIONS;
  }

  const candidates = [
    enums.noteType,
    enums.NOTE_TYPE,
    enums.NoteType,
    enums.noteTypes,
    enums.NOTE_TYPES,
  ];
  const source = candidates.find((item) => Array.isArray(item)) || [];
  const options = source.map(normalizeEnumOption).filter(Boolean);
  return options.length ? options : DEFAULT_NOTE_TYPE_OPTIONS;
}

function toContentText(content) {
  if (content == null) {
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
}

export default {
  name: "NoteEditView",
  data() {
    return {
      loading: false,
      errorMessage: "",
      form: {
        id: "",
        parentId: "",
        title: "",
        noteType: "",
        sort: 0,
        icon: "",
        tagsText: "",
        contentText: "",
      },
    };
  },
  computed: {
    noteTypeOptions() {
      return resolveNoteTypeOptions(this.$store.getters.globalEnums);
    },
  },
  async created() {
    try {
      await this.$store.dispatch("fetchGlobalEnums");
    } catch (error) {
      // 枚举加载失败时使用本地兜底，不影响详情加载。
      console.warn("[NoteEditView] fetchGlobalEnums failed:", error);
    }
    await this.loadDetail();
  },
  watch: {
    "$route.fullPath"() {
      this.loadDetail();
    },
  },
  methods: {
    resolveNoteId() {
      const routeId = this.$route.params.id || this.$route.query.id;
      const parsed = Number(routeId);
      return Number.isNaN(parsed) ? null : parsed;
    },
    normalizeNoteTypeValue(noteType) {
      if (typeof noteType === "number") {
        return noteType;
      }
      if (typeof noteType === "string") {
        const asNumber = Number(noteType);
        if (!Number.isNaN(asNumber)) {
          return asNumber;
        }
        const map = {
          MARKDOWN: 1,
          WORD_CARD: 2,
          SENTENCE: 3,
          QUESTIONS: 4,
        };
        if (Object.prototype.hasOwnProperty.call(map, noteType)) {
          return map[noteType];
        }
      }
      return "";
    },
    async loadDetail() {
      this.loading = true;
      this.errorMessage = "";
      try {
        const id = this.resolveNoteId();
        if (!id) {
          this.errorMessage = "缺少节点ID，无法进入编辑页。";
          return;
        }
        const vo = await getNoteNodeById(id);
        const noteNode = normalizeNoteNode(
          vo?.noteNode || createDefaultNoteNode()
        );
        const content = vo?.content ?? noteNode.content;
        this.form = {
          id: noteNode.id || "",
          parentId: noteNode.parentId ?? "",
          title: noteNode.title || "",
          noteType: this.normalizeNoteTypeValue(noteNode.noteType),
          sort: Number(noteNode.sort || 0),
          icon: noteNode.meta.icon || "",
          tagsText: (noteNode.meta.tags || [])
            .map((item) => item.name)
            .join(", "),
          contentText: toContentText(content),
        };
      } catch (error) {
        this.errorMessage = `加载失败：${error.message}`;
      } finally {
        this.loading = false;
      }
    },
    handleSave() {
      this.errorMessage = "提交逻辑暂未实现。";
    },
    goBack() {
      const id = this.resolveNoteId();
      if (id) {
        this.$router.push({
          name: "note",
          params: { id: String(id) },
        });
        return;
      }
      this.$router.back();
    },
  },
};
</script>

<style scoped>
* {
  box-sizing: border-box;
}

.note-edit-page {
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

.card {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 6px 20px rgba(15, 23, 42, 0.06);
  padding: 20px;
  margin-bottom: 16px;
}

.header-top {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 12px;
  align-items: center;
}

.title {
  margin: 0;
  text-align: center;
  font-size: 24px;
  color: #111827;
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
}

.save-btn {
  height: 38px;
  border: 1px solid #2563eb;
  border-radius: 10px;
  background: #2563eb;
  color: #fff;
  padding: 0 12px;
  font-size: 14px;
  cursor: pointer;
}

.status {
  margin-top: 12px;
  font-size: 13px;
  color: #6b7280;
}

.status.error {
  color: #b91c1c;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field.block {
  margin-top: 16px;
}

.label {
  font-size: 13px;
  color: #4b5563;
}

input,
select,
textarea {
  width: 100%;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 10px 12px;
  font-size: 14px;
  color: #111827;
  background: #fff;
}

textarea {
  resize: vertical;
  min-height: 220px;
}

@media (max-width: 680px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .header-top {
    grid-template-columns: auto 1fr;
  }

  .save-btn {
    grid-column: 1 / -1;
  }
}
</style>
