<template>
  <div class="mobile-page">
    <main class="content-scroll">
      <section class="card">
        <div class="header-row">
          <h1 class="title">{{ pageTitle }}</h1>
          <button
            type="button"
            class="back-btn"
            title="返回"
            aria-label="返回"
            @click="goBack"
          >
            ↩
          </button>
        </div>
        <p class="sub">{{ pageSubTitle }}</p>
        <p v-if="loading" class="status">正在加载节点数据...</p>
        <p v-else-if="errorMessage" class="status status-error">
          {{ errorMessage }}
        </p>
      </section>

      <section class="card">
        <div class="form-grid">
          <label class="field">
            <span class="label">节点名称</span>
            <input
              v-model.trim="form.title"
              class="input"
              type="text"
              maxlength="30"
              placeholder="请输入节点名称"
            />
            <span class="hint">必填，最大 30 字符</span>
          </label>

          <label class="field">
            <span class="label">选择父节点（可选）</span>
            <div class="search-combobox" ref="parentCombobox">
              <input
                v-model.trim="parentKeyword"
                class="input"
                type="text"
                placeholder="搜索父节点"
                autocomplete="off"
                @focus="handleParentFocus"
                @input="handleParentInput"
                @keydown.enter.prevent="handleParentEnter"
              />
              <ul class="search-dropdown" :class="{ show: showParentDropdown }">
                <li>
                  <button
                    type="button"
                    class="search-option"
                    @click="clearParentNode"
                  >
                    不设置父节点
                  </button>
                </li>
                <li v-for="item in parentOptions" :key="item.id">
                  <button
                    type="button"
                    class="search-option"
                    @click="selectParentNode(item)"
                  >
                    {{ item.title }}
                  </button>
                </li>
              </ul>
            </div>
            <span class="hint">可留空；新增完成后默认返回父节点详情。</span>
          </label>

          <label class="field">
            <span class="label">标签</span>
            <div class="tag-editor">
              <div class="tag-list">
                <span
                  v-for="(tag, index) in form.tags"
                  :key="`${tag.label}-${index}`"
                  class="tag-chip app-tag"
                  :class="tag.className || FALLBACK_TAG_CLASS_NAME"
                >
                  {{ tag.label }}
                  <button
                    type="button"
                    class="tag-remove"
                    aria-label="删除标签"
                    @click.stop="removeTag(index)"
                  >
                    ×
                  </button>
                </span>
                <span v-if="!form.tags.length" class="hint">暂无标签</span>
              </div>

              <div class="tag-input-row">
                <div class="tag-combobox" ref="tagCombobox">
                  <input
                    v-model.trim="tagKeyword"
                    class="input"
                    type="text"
                    placeholder="搜索标签"
                    autocomplete="off"
                    @focus="handleTagFocus"
                    @input="handleTagInput"
                    @keydown.enter.prevent="handleTagEnter"
                  />
                  <ul class="tag-dropdown" :class="{ show: showTagDropdown }">
                    <li v-for="item in tagOptions" :key="item.id || item.label">
                      <button
                        type="button"
                        class="tag-option"
                        @click="selectTagOption(item)"
                      >
                        <span
                          class="app-tag"
                          :class="item.className || FALLBACK_TAG_CLASS_NAME"
                        >
                          {{ item.label }}
                        </span>
                      </button>
                    </li>
                    <li v-if="canCreateTagFromKeyword">
                      <button
                        type="button"
                        class="tag-option"
                        @click="openCreateTagModal"
                      >
                        新增标签：{{ normalizedTagKeyword }}
                      </button>
                    </li>
                  </ul>
                </div>
              </div>
            </div>
            <span class="hint"
              >先搜索；无结果时可新增，新增时必须选择标签样式。</span
            >
          </label>

          <div class="inline-row">
            <label class="field">
              <span class="label">类型</span>
              <select
                v-model.number="form.noteTypeCode"
                class="select"
                :disabled="!isCreateMode"
              >
                <option disabled :value="null">请选择类型</option>
                <option
                  v-for="item in noteTypeOptions"
                  :key="item.code"
                  :value="item.code"
                >
                  {{ item.label }}
                </option>
              </select>
              <span v-if="!isCreateMode" class="hint"
                >编辑模式不允许修改类型</span
              >
            </label>

            <label class="field">
              <span class="label">顺序</span>
              <input
                v-model.number="form.sort"
                class="input"
                type="number"
                min="0"
                step="1"
                placeholder="100"
              />
              <span class="hint">默认 100</span>
            </label>
          </div>

          <label class="field">
            <span class="label">内容（可选，JSON 或文本）</span>
            <textarea
              v-model="form.contentText"
              class="input textarea"
              rows="6"
              placeholder="可留空"
            />
          </label>
        </div>
      </section>
    </main>

    <div class="editor-actions-fixed">
      <div class="action-row">
        <button type="button" class="btn secondary" @click="goBack">
          取消
        </button>
        <button
          type="button"
          class="btn primary"
          :disabled="loading || saving"
          @click="handleSave"
        >
          {{ saving ? "保存中..." : "保存" }}
        </button>
      </div>
    </div>

    <div
      v-if="showTagCreateModal"
      class="tag-modal-mask"
      role="dialog"
      aria-modal="true"
      @click.self="closeTagCreateModal"
    >
      <div class="tag-modal">
        <div class="tag-modal-header">
          <h3 class="tag-modal-title">新增标签样式选择</h3>
          <button
            type="button"
            class="tag-modal-close"
            aria-label="关闭"
            @click="closeTagCreateModal"
          >
            ×
          </button>
        </div>
        <div class="tag-modal-content">
          <div class="tag-modal-preview">
            <span>实时预览：</span>
            <span class="app-tag" :class="pendingNewTagClassName">
              {{ pendingNewTagLabel }}
            </span>
          </div>
          <div class="tag-style-grid">
            <button
              v-for="preset in TAG_STYLE_PRESETS"
              :key="preset.className"
              type="button"
              class="tag-style-item"
              :class="{
                active: pendingNewTagClassName === preset.className,
              }"
              @click="pendingNewTagClassName = preset.className"
            >
              <span class="app-tag" :class="preset.className">
                {{ preset.label }}
              </span>
            </button>
          </div>
        </div>
        <div class="tag-modal-actions">
          <button
            type="button"
            class="btn secondary"
            @click="closeTagCreateModal"
          >
            取消
          </button>
          <button
            type="button"
            class="btn primary"
            :disabled="saving"
            @click="confirmCreateTag"
          >
            确认新增
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {
  createNoteNodeByDraft,
  createNoteTagOption,
  fetchNoteNodeDetail,
  fetchParentNoteNodeDisplay,
  searchNoteTagOptions,
  searchParentNoteNodeOptions,
  updateNoteNodeByDraft,
} from "@/views/domain/language-jp/services/note-node.service";
import {
  DEFAULT_TAG_CLASS_NAME,
  TAG_STYLE_PRESETS,
} from "@/constants/tagStylePresets";

const NOTE_TYPE_CODE_TO_KEY = Object.freeze({
  0: "EMPTY",
  1: "MARKDOWN",
  2: "WORD_CARD",
  3: "SENTENCE",
  4: "ARTICLE",
  100: "PRACTICE",
});

const NOTE_TYPE_FALLBACK_OPTIONS = Object.freeze([
  { code: 1, enumKey: "MARKDOWN", label: "Markdown (1)" },
  { code: 2, enumKey: "WORD_CARD", label: "Word Card (2)" },
  { code: 3, enumKey: "SENTENCE", label: "Sentence (3)" },
  { code: 4, enumKey: "ARTICLE", label: "Article (4)" },
  { code: 100, enumKey: "PRACTICE", label: "Practice (100)" },
]);

function parsePositiveInt(value) {
  if (value === undefined || value === null || value === "") {
    return null;
  }
  const parsed = Number(value);
  if (!Number.isInteger(parsed) || parsed <= 0) {
    return null;
  }
  return parsed;
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

function parseContent(value) {
  const text = String(value || "").trim();
  if (!text) {
    return null;
  }
  try {
    return JSON.parse(text);
  } catch (error) {
    return text;
  }
}

export default {
  name: "NoteEditView",
  data() {
    return {
      TAG_STYLE_PRESETS,
      FALLBACK_TAG_CLASS_NAME: "app-tag--info",
      loading: false,
      saving: false,
      errorMessage: "",
      form: {
        id: null,
        parentId: null,
        title: "",
        noteTypeCode: null,
        sort: 100,
        contentText: "",
        icon: "",
        subject: "",
        tags: [],
      },
      parentKeyword: "",
      parentOptions: [],
      showParentDropdown: false,
      parentSearchSeq: 0,
      tagKeyword: "",
      tagOptions: [],
      showTagDropdown: false,
      tagSearchSeq: 0,
      showTagCreateModal: false,
      pendingNewTagLabel: "",
      pendingNewTagClassName: DEFAULT_TAG_CLASS_NAME,
    };
  },
  computed: {
    isCreateMode() {
      return this.$route.name === "language-jp-materials-new";
    },
    pageTitle() {
      return this.isCreateMode ? "节点新增" : "节点编辑";
    },
    pageSubTitle() {
      return this.isCreateMode
        ? "统一用于节点新增。保存后返回父节点详情。"
        : "统一用于节点编辑。编辑模式不允许修改节点类型。";
    },
    routeNodeId() {
      return parsePositiveInt(this.$route.params.id);
    },
    routeParentId() {
      return parsePositiveInt(this.$route.query.parentId);
    },
    normalizedTagKeyword() {
      return String(this.tagKeyword || "").trim();
    },
    noteTypeOptions() {
      const source = this.$store.getters["app/enumByKey"]("noteType");
      if (!Array.isArray(source) || !source.length) {
        return NOTE_TYPE_FALLBACK_OPTIONS;
      }

      const options = source
        .map((item) => {
          if (!item || typeof item !== "object") {
            return null;
          }
          const codeRaw = [item.code, item.value, item.id].find(
            (x) => x !== undefined && x !== null && x !== ""
          );
          const code = Number(codeRaw);
          if (!Number.isInteger(code)) {
            return null;
          }

          const enumKey =
            item.key ||
            item.enumKey ||
            item.enumName ||
            NOTE_TYPE_CODE_TO_KEY[code] ||
            "";
          const name = item.name || item.label || enumKey || `Type-${code}`;
          const desc = item.desc || item.description || "";
          const label = desc
            ? `${name} (${code}) - ${desc}`
            : `${name} (${code})`;

          return {
            code,
            enumKey,
            label,
          };
        })
        .filter(Boolean)
        .filter(
          (item, index, arr) =>
            arr.findIndex((v) => v.code === item.code) === index
        );

      return options.length ? options : NOTE_TYPE_FALLBACK_OPTIONS;
    },
    canCreateTagFromKeyword() {
      if (!this.normalizedTagKeyword) {
        return false;
      }
      const normalized = this.normalizedTagKeyword.toLowerCase();
      const existsInSelected = this.form.tags.some(
        (item) =>
          String(item.label || "")
            .trim()
            .toLowerCase() === normalized
      );
      if (existsInSelected) {
        return false;
      }
      const existsInSearch = this.tagOptions.some(
        (item) =>
          String(item.label || "")
            .trim()
            .toLowerCase() === normalized
      );
      return !existsInSearch;
    },
  },
  async created() {
    await this.loadPageData();
  },
  mounted() {
    document.addEventListener("click", this.handleGlobalClick);
  },
  beforeDestroy() {
    document.removeEventListener("click", this.handleGlobalClick);
  },
  watch: {
    "$route.fullPath"() {
      this.loadPageData();
    },
  },
  methods: {
    async loadPageData() {
      this.loading = true;
      this.errorMessage = "";
      try {
        await this.$store.dispatch("app/fetchGlobalEnums");
        if (this.isCreateMode) {
          await this.loadCreateMode();
        } else {
          await this.loadEditMode();
        }
      } catch (error) {
        this.errorMessage = `加载失败：${error.message}`;
      } finally {
        this.loading = false;
      }
    },
    resetForm() {
      this.form = {
        id: null,
        parentId: null,
        title: "",
        noteTypeCode: this.resolveDefaultNoteTypeCode(),
        sort: 100,
        contentText: "",
        icon: "",
        subject: "",
        tags: [],
      };
      this.parentKeyword = "";
      this.parentOptions = [];
      this.showParentDropdown = false;
      this.tagKeyword = "";
      this.tagOptions = [];
      this.showTagDropdown = false;
      this.showTagCreateModal = false;
      this.pendingNewTagLabel = "";
      this.pendingNewTagClassName = DEFAULT_TAG_CLASS_NAME;
    },
    async loadCreateMode() {
      this.resetForm();
      const parentId = this.routeParentId;
      this.form.parentId = parentId;
      if (parentId) {
        await this.loadParentDisplay(parentId);
      }
    },
    async loadEditMode() {
      this.resetForm();
      const id = this.routeNodeId;
      if (!id) {
        this.errorMessage = "缺少节点ID，无法进入编辑页。";
        return;
      }

      const detail = await fetchNoteNodeDetail(id);
      const noteNode = detail?.noteNode || {};
      const noteTypeCode = this.resolveNoteTypeCode(noteNode.noteType);

      this.form.id = id;
      this.form.parentId = parsePositiveInt(noteNode.parentId);
      this.form.title = noteNode.title || "";
      this.form.noteTypeCode =
        noteTypeCode ?? this.resolveDefaultNoteTypeCode();
      this.form.sort = Number.isFinite(Number(noteNode.sort))
        ? Number(noteNode.sort)
        : 100;
      this.form.icon = noteNode?.meta?.icon || "";
      this.form.subject = noteNode?.meta?.subject || "";
      this.form.tags = this.normalizeTags(noteNode?.meta?.tags || []);
      this.form.contentText = toContentText(detail?.content);

      if (this.form.parentId) {
        await this.loadParentDisplay(this.form.parentId);
      }
    },
    async loadParentDisplay(parentId) {
      try {
        this.parentKeyword = await fetchParentNoteNodeDisplay(parentId);
      } catch (error) {
        this.parentKeyword = `节点 ${parentId}`;
      }
    },
    normalizeTags(tags) {
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
            bizType: item?.bizType || "NOTE",
            label,
            className: item?.className || this.FALLBACK_TAG_CLASS_NAME,
          };
        })
        .filter(Boolean);
    },
    resolveDefaultNoteTypeCode() {
      const markdown = this.noteTypeOptions.find((item) => item.code === 1);
      if (markdown) {
        return markdown.code;
      }
      return this.noteTypeOptions[0]?.code ?? null;
    },
    resolveNoteTypeCode(noteType) {
      if (Number.isInteger(Number(noteType))) {
        return Number(noteType);
      }
      if (typeof noteType === "string" && noteType.trim()) {
        let normalized = noteType.trim().toUpperCase();
        if (normalized === "QUESTIONS" || normalized === "QUESTION") {
          normalized = "PRACTICE";
        }
        const found = this.noteTypeOptions.find(
          (item) => item.enumKey && item.enumKey.toUpperCase() === normalized
        );
        if (found) {
          return found.code;
        }
      }
      return null;
    },
    resolveNoteTypeKey(code) {
      const found = this.noteTypeOptions.find(
        (item) => item.code === Number(code)
      );
      if (found?.enumKey) {
        return found.enumKey;
      }
      return NOTE_TYPE_CODE_TO_KEY[Number(code)] || "";
    },
    async handleParentFocus() {
      await this.searchParentOptions(this.parentKeyword);
      this.showParentDropdown = true;
    },
    async handleParentInput() {
      this.form.parentId = null;
      await this.searchParentOptions(this.parentKeyword);
      this.showParentDropdown = true;
    },
    async handleParentEnter() {
      if (this.parentOptions.length) {
        this.selectParentNode(this.parentOptions[0]);
      }
    },
    async searchParentOptions(keyword) {
      const seq = ++this.parentSearchSeq;
      const list = await searchParentNoteNodeOptions({
        keyword: String(keyword || "").trim(),
        excludeId: this.isCreateMode ? null : this.form.id,
        limit: 20,
      });
      if (seq !== this.parentSearchSeq) {
        return;
      }
      this.parentOptions = Array.isArray(list) ? list : [];
    },
    selectParentNode(item) {
      if (!item || !item.id) {
        return;
      }
      this.form.parentId = Number(item.id);
      this.parentKeyword = item.title;
      this.showParentDropdown = false;
    },
    clearParentNode() {
      this.form.parentId = null;
      this.parentKeyword = "";
      this.showParentDropdown = false;
    },
    async handleTagFocus() {
      await this.searchTagOptions(this.tagKeyword);
      this.showTagDropdown = true;
    },
    async handleTagInput() {
      await this.searchTagOptions(this.tagKeyword);
      this.showTagDropdown = true;
    },
    async handleTagEnter() {
      if (this.tagOptions.length) {
        this.selectTagOption(this.tagOptions[0]);
        return;
      }
      if (this.canCreateTagFromKeyword) {
        this.openCreateTagModal();
      }
    },
    async searchTagOptions(keyword) {
      const seq = ++this.tagSearchSeq;
      const list = await searchNoteTagOptions({
        keyword: String(keyword || "").trim(),
        limit: 20,
      });
      if (seq !== this.tagSearchSeq) {
        return;
      }

      const selectedLabels = new Set(
        this.form.tags.map((item) => item.label.toLowerCase())
      );

      this.tagOptions = (Array.isArray(list) ? list : [])
        .map((item) => {
          const label = String(item?.label || "").trim();
          if (!label) {
            return null;
          }
          return {
            id: item?.id || null,
            bizType: item?.bizType || "NOTE",
            label,
            className: item?.className || this.FALLBACK_TAG_CLASS_NAME,
          };
        })
        .filter(Boolean)
        .filter((item) => !selectedLabels.has(item.label.toLowerCase()));
    },
    selectTagOption(item) {
      if (!item || !item.label) {
        return;
      }
      const exists = this.form.tags.some(
        (tag) => tag.label.toLowerCase() === item.label.toLowerCase()
      );
      if (exists) {
        return;
      }
      this.form.tags.push({
        id: item.id || null,
        bizType: item.bizType || "NOTE",
        label: item.label,
        className: item.className || this.FALLBACK_TAG_CLASS_NAME,
      });
      this.tagKeyword = "";
      this.showTagDropdown = false;
    },
    openCreateTagModal() {
      if (!this.canCreateTagFromKeyword) {
        return;
      }
      this.pendingNewTagLabel = this.normalizedTagKeyword;
      this.pendingNewTagClassName = DEFAULT_TAG_CLASS_NAME;
      this.showTagCreateModal = true;
      this.showTagDropdown = false;
    },
    closeTagCreateModal() {
      this.showTagCreateModal = false;
      this.pendingNewTagLabel = "";
      this.pendingNewTagClassName = DEFAULT_TAG_CLASS_NAME;
    },
    async confirmCreateTag() {
      const label = String(this.pendingNewTagLabel || "").trim();
      if (!label) {
        this.errorMessage = "标签名称不能为空。";
        return;
      }
      this.errorMessage = "";
      const created = await createNoteTagOption({
        label,
        className: this.pendingNewTagClassName || DEFAULT_TAG_CLASS_NAME,
      });
      this.selectTagOption({
        id: created?.id || null,
        bizType: created?.bizType || "NOTE",
        label: created?.label || label,
        className:
          created?.className ||
          this.pendingNewTagClassName ||
          DEFAULT_TAG_CLASS_NAME,
      });
      this.closeTagCreateModal();
    },
    removeTag(index) {
      this.form.tags.splice(index, 1);
    },
    validateForm() {
      const title = String(this.form.title || "").trim();
      if (!title) {
        return "标题必填。";
      }
      if (title.length > 30) {
        return "标题长度不能超过 30 字符。";
      }
      if (!Number.isInteger(Number(this.form.sort))) {
        return "顺序必须是整数。";
      }
      if (Number(this.form.sort) < 0) {
        return "顺序不能小于 0。";
      }
      if (this.form.noteTypeCode == null) {
        return "请选择节点类型。";
      }
      const noteTypeKey = this.resolveNoteTypeKey(this.form.noteTypeCode);
      if (!noteTypeKey) {
        return "节点类型无效，请刷新后重试。";
      }
      return "";
    },
    buildPayload() {
      const noteType = this.resolveNoteTypeKey(this.form.noteTypeCode);
      return {
        noteNode: {
          parentId: this.form.parentId,
          title: String(this.form.title || "").trim(),
          noteType,
          sort: Number(this.form.sort),
          meta: {
            icon: this.form.icon || "",
            subject: this.form.subject || "",
            tags: this.form.tags.map((item) => ({
              id: item.id || undefined,
              bizType: "NOTE",
              label: item.label,
              className: item.className || this.FALLBACK_TAG_CLASS_NAME,
            })),
          },
        },
        content: parseContent(this.form.contentText),
      };
    },
    async handleSave() {
      const validationError = this.validateForm();
      if (validationError) {
        this.errorMessage = validationError;
        return;
      }

      this.saving = true;
      this.errorMessage = "";
      try {
        const payload = this.buildPayload();
        if (this.isCreateMode) {
          await createNoteNodeByDraft(payload);
          this.$store.dispatch("app/showToast", {
            type: "info",
            message: "新增成功",
          });
          if (this.form.parentId) {
            this.$router.push({
              name: "language-jp-materials",
              params: { id: String(this.form.parentId) },
            });
          } else {
            this.$router.push({ name: "language-jp-home" });
          }
          return;
        }

        await updateNoteNodeByDraft(this.form.id, payload);
        this.$store.dispatch("app/showToast", {
          type: "info",
          message: "保存成功",
        });
        this.$router.push({
          name: "language-jp-materials",
          params: { id: String(this.form.id) },
        });
      } catch (error) {
        this.errorMessage = error?.message || "保存失败，请稍后重试。";
      } finally {
        this.saving = false;
      }
    },
    goBack() {
      if (this.isCreateMode) {
        if (this.form.parentId) {
          this.$router.push({
            name: "language-jp-materials",
            params: { id: String(this.form.parentId) },
          });
          return;
        }
        this.$router.push({ name: "language-jp-home" });
        return;
      }

      if (this.form.id) {
        this.$router.push({
          name: "language-jp-materials",
          params: { id: String(this.form.id) },
        });
        return;
      }
      this.$router.back();
    },
    handleGlobalClick(event) {
      const parentBox = this.$refs.parentCombobox;
      if (parentBox && !parentBox.contains(event.target)) {
        this.showParentDropdown = false;
      }
      const tagBox = this.$refs.tagCombobox;
      if (tagBox && !tagBox.contains(event.target)) {
        this.showTagDropdown = false;
      }
    },
  },
};
</script>

<style scoped>
* {
  box-sizing: border-box;
}

.mobile-page {
  --bg: #f5f7fb;
  --card: #ffffff;
  --text: #1f2937;
  --muted: #6b7280;
  --line: #e5e7eb;
  --brand: #1d4ed8;
  --input-line: #dbe1ea;
  --card-line: #edf2f7;
  position: relative;
  width: 100%;
  height: 100dvh;
  min-height: -webkit-fill-available;
  overflow: hidden;
  background: var(--bg);
  color: var(--text);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC",
    "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
}

.content-scroll {
  height: calc(100dvh - 56px - env(safe-area-inset-bottom));
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  padding: calc(14px + env(safe-area-inset-top)) 12px
    calc(132px + env(safe-area-inset-bottom));
}

.card {
  background: var(--card);
  border: 1px solid var(--card-line);
  border-radius: 16px;
  box-shadow: 0 6px 20px rgba(15, 23, 42, 0.06);
  padding: 16px;
  margin-bottom: 12px;
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
}

.title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #111827;
}

.back-btn {
  width: 34px;
  height: 34px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: #f8fafc;
  color: #374151;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  cursor: pointer;
}

.sub {
  margin: 0;
  font-size: 12px;
  color: var(--muted);
  line-height: 1.5;
}

.status {
  margin: 10px 0 0;
  font-size: 12px;
  color: var(--text);
}

.status-error {
  color: var(--color-error-text);
}

.form-grid {
  display: grid;
  gap: 12px;
}

.field {
  display: grid;
  gap: 6px;
}

.label {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
}

.input,
.select {
  width: 100%;
  border: 1px solid var(--input-line);
  border-radius: 10px;
  background: #fff;
  color: #111827;
  font-size: 14px;
  padding: 10px 12px;
  outline: none;
}

.input:focus,
.select:focus {
  border-color: #93c5fd;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.12);
}

.input:disabled,
.select:disabled {
  background: #f8fafc;
  color: var(--muted);
}

.textarea {
  resize: vertical;
  min-height: 96px;
}

.hint {
  font-size: 12px;
  color: var(--muted);
  line-height: 1.45;
}

.inline-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.search-combobox,
.tag-combobox {
  position: relative;
}

.search-dropdown,
.tag-dropdown {
  position: absolute;
  left: 0;
  right: 0;
  top: calc(100% + 4px);
  z-index: 20;
  margin: 0;
  padding: 4px;
  list-style: none;
  border: 1px solid var(--input-line);
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08);
  max-height: 180px;
  overflow-y: auto;
  display: none;
}

.search-dropdown.show,
.tag-dropdown.show {
  display: block;
}

.search-option,
.tag-option {
  width: 100%;
  border: 0;
  background: transparent;
  text-align: left;
  font-size: 13px;
  color: #334155;
  padding: 8px 10px;
  border-radius: 8px;
  cursor: pointer;
}

.search-option:hover,
.tag-option:hover {
  background: #eff6ff;
  color: #1d4ed8;
}

.tag-editor {
  display: grid;
  gap: 8px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  min-height: 24px;
}

.tag-chip {
  gap: 6px;
}

.tag-chip.app-tag {
  --app-tag-color: #1d4ed8;
  --app-tag-bg: #eff6ff;
  --app-tag-border: #dbeafe;
  font-size: 12px;
  padding: 3px 8px;
}

.tag-remove {
  border: 0;
  background: transparent;
  color: #64748b;
  cursor: pointer;
  font-size: 12px;
  line-height: 1;
  padding: 0;
}

.tag-remove:hover {
  color: #334155;
}

.tag-input-row {
  display: grid;
  grid-template-columns: 1fr;
  gap: 8px;
}

.editor-actions-fixed {
  position: fixed;
  left: 0;
  right: 0;
  bottom: calc(56px + env(safe-area-inset-bottom));
  z-index: 1001;
  padding: 8px 12px;
  background: rgba(245, 247, 251, 0.95);
  backdrop-filter: blur(8px);
}

.action-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.btn {
  height: 40px;
  border-radius: 10px;
  border: 1px solid transparent;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.btn.secondary {
  background: #f8fafc;
  border-color: var(--input-line);
  color: #334155;
}

.btn.primary {
  background: var(--brand);
  border-color: var(--brand);
  color: #fff;
}

.btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.tag-modal-mask {
  position: fixed;
  inset: 0;
  z-index: 1200;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
}

.tag-modal {
  width: min(520px, 100%);
  max-height: min(80vh, 700px);
  overflow: auto;
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: 16px;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.2);
}

.tag-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 12px 14px 8px;
  border-bottom: 1px solid var(--line);
}

.tag-modal-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
}

.tag-modal-close {
  width: 28px;
  height: 28px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: var(--color-surface-page);
  color: var(--text);
  font-size: 14px;
  cursor: pointer;
}

.tag-modal-content {
  padding: 12px 14px;
}

.tag-modal-preview {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 13px;
  color: var(--muted);
}

.tag-style-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.tag-style-item {
  border: 1px solid var(--line);
  border-radius: 10px;
  background: var(--card);
  padding: 8px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  cursor: pointer;
}

.tag-style-item.active {
  border-color: var(--brand);
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.12);
}

.tag-modal-actions {
  padding: 10px 14px 14px;
  border-top: 1px solid var(--line);
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

@media (max-width: 420px) {
  .inline-row,
  .tag-style-grid {
    grid-template-columns: 1fr;
  }
}

@media (min-width: 768px) {
  .mobile-page {
    max-width: 430px;
    margin: 0 auto;
    border-left: 1px solid var(--line);
    border-right: 1px solid var(--line);
  }

  .editor-actions-fixed {
    max-width: 430px;
    margin: 0 auto;
    left: 50%;
    transform: translateX(-50%);
    padding-left: 12px;
    padding-right: 12px;
  }
}
</style>
