<template>
  <div class="page">
    <div class="exam-shell">
      <div class="exam-header">
        <h1 class="exam-title">{{ headerTitle }}</h1>
        <div class="exam-meta">
          <span class="meta-tag">{{ modeLabel }}</span>
          <span class="meta-tag">{{ typeSummary }}</span>
          <span class="meta-tag">风格：接近日语考试练习</span>
        </div>

        <div v-if="session" class="progress-wrap">
          <div class="progress-text">
            第 {{ displayIndex }} / {{ session.totalCount || 0 }} 题
          </div>
          <div class="progress-bar">
            <div class="progress-inner" :style="{ width: progressWidth }" />
          </div>
        </div>
      </div>

      <div class="question-card">
        <div v-if="nodeId === null" class="empty-state">
          <div class="empty-title">缺少 nodeId（题目节点ID）</div>
          <div class="empty-desc">
            请在下方输入题目节点 ID（NoteType=QUESTIONS），用于创建练习会话。
          </div>
          <div class="node-input-row">
            <input
              v-model.trim="nodeIdInput"
              class="node-input"
              placeholder="例如：10001"
              inputmode="numeric"
            />
            <button class="btn btn-primary" @click="applyNodeId">开始</button>
          </div>
        </div>

        <template v-else>
          <div v-if="loading" class="loading">加载中...</div>
          <div v-else-if="error" class="error">{{ error }}</div>
          <div v-else-if="!session || !question" class="empty-state">
            <div class="empty-title">暂无题目</div>
            <div class="empty-desc">
              请确认该 nodeId 已绑定题目（question_note_node_rel），或稍后再试。
            </div>
            <button class="btn btn-default" @click="startSession">
              重新开始
            </button>
          </div>
          <template v-else>
            <div class="question-top">
              <div class="question-type">{{ typeLabel }}</div>
              <div class="question-score">分值：{{ score }} 分</div>
            </div>

            <div class="question-section">{{ sectionLabel }}</div>
            <div class="question-title">{{ question.title }}</div>
            <div class="question-desc">{{ question.stem }}</div>

            <div v-if="audioUrl" class="listening-box">
              <div class="listening-label">听力音频</div>
              <audio :key="audioUrl" controls>
                <source :src="audioUrl" type="audio/mpeg" />
                您的浏览器不支持 audio 标签
              </audio>
            </div>

            <div v-if="isChoice" class="options">
              <div
                v-for="opt in normalizedOptions"
                :key="opt.key"
                class="option-item"
                :class="{ active: isOptionSelected(opt.key) }"
                @click="toggleOption(opt.key)"
              >
                <label class="option-label">
                  <input
                    :type="isMultipleChoice ? 'checkbox' : 'radio'"
                    name="option"
                    :value="opt.key"
                    :checked="isOptionSelected(opt.key)"
                    @change.prevent
                  />
                  <span class="option-index">{{ opt.index }}</span>
                  <span class="option-text">{{ opt.text }}</span>
                </label>
              </div>
            </div>

            <div v-else class="fill-box">
              <div class="fill-tip">{{ fillTip }}</div>
              <template v-if="blankKeys.length > 1">
                <input
                  v-for="blankKey in blankKeys"
                  :key="blankKey"
                  v-model.trim="blankAnswers[blankKey]"
                  type="text"
                  class="fill-input"
                  :placeholder="blankKey"
                />
              </template>
              <input
                v-else
                v-model.trim="fillValue"
                type="text"
                class="fill-input"
                placeholder="请输入答案"
              />
            </div>

            <div
              v-if="resultBox.visible"
              class="result-box show"
              :class="resultBox.correct ? 'correct' : 'wrong'"
              v-html="resultBox.html"
            />
          </template>
        </template>
      </div>

      <div class="exam-footer">
        <div class="action-row">
          <button class="btn btn-default" :disabled="!canPrev" @click="prev">
            上一题
          </button>
          <button
            class="btn btn-success"
            :disabled="!canSubmit"
            @click="submit"
          >
            提交答案
          </button>
          <button class="btn btn-primary" :disabled="!canNext" @click="next">
            下一题
          </button>
        </div>
        <div class="summary">
          已作答：{{ session?.answeredCount || 0 }} /
          {{ session?.totalCount || 0 }}，答对：{{ session?.correctCount || 0 }}
          题
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {
  fetchPracticeItem,
  startPracticeSession,
  submitPractice,
} from "@/views/domain/language-jp/services/practice.service";
import { getLastLanguageJpNoteId } from "@/utils/languageJpNav";
import {
  getLastLanguageJpPracticeNodeId,
  saveLastLanguageJpPracticeNodeId,
} from "@/utils/languageJpPracticeNav";

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

function randomClientId() {
  return `c_${Date.now()}_${Math.random().toString(16).slice(2)}`;
}

export default {
  name: "PracticeExamIndexView",
  data() {
    return {
      loading: false,
      error: "",
      nodeIdInput: "",

      session: null,
      question: null,
      localIndex: 0,

      selectedOptionKeys: [],
      fillValue: "",
      blankAnswers: {},

      resultBox: {
        visible: false,
        correct: false,
        html: "",
      },
    };
  },
  computed: {
    parentId() {
      const fallback = getLastLanguageJpNoteId();
      return parsePositiveInt(this.$route.params.parentId) || fallback;
    },
    nodeId() {
      const queryNodeId = parsePositiveInt(this.$route.query.nodeId);
      if (queryNodeId) {
        return queryNodeId;
      }
      const fallback = getLastLanguageJpPracticeNodeId(this.parentId);
      return parsePositiveInt(fallback);
    },
    headerTitle() {
      return "日本語能力試験 N3 模拟练习";
    },
    modeLabel() {
      return "一题一题模式";
    },
    typeSummary() {
      return "题型：选择 / 听力 / 填空";
    },
    displayIndex() {
      const idx = Number(this.localIndex);
      if (!Number.isInteger(idx) || idx < 0) {
        return 1;
      }
      return idx + 1;
    },
    progressWidth() {
      const total = this.session?.totalCount || 0;
      if (!total) {
        return "0%";
      }
      return `${(this.displayIndex / total) * 100}%`;
    },
    questionMeta() {
      return this.question?.metaInfo || {};
    },
    displayMeta() {
      return this.questionMeta?.display || {};
    },
    typeLabel() {
      return (
        this.displayMeta?.typeLabel ||
        this.fallbackTypeLabel(this.question?.questionType)
      );
    },
    sectionLabel() {
      return this.displayMeta?.section || "练习";
    },
    score() {
      const value = Number(this.displayMeta?.score);
      if (Number.isFinite(value) && value > 0) {
        return value;
      }
      return 1;
    },
    audioUrl() {
      const assets = Array.isArray(this.questionMeta?.assets)
        ? this.questionMeta.assets
        : [];
      const audio = assets.find((it) => it && it.type === "audio" && it.url);
      return audio ? audio.url : "";
    },
    isMultipleChoice() {
      return this.question?.questionType === "MULTIPLE_CHOICE";
    },
    isChoice() {
      const t = this.question?.questionType;
      return (
        t === "SINGLE_CHOICE" || t === "MULTIPLE_CHOICE" || t === "TRUE_FALSE"
      );
    },
    normalizedOptions() {
      const raw = Array.isArray(this.questionMeta?.options)
        ? this.questionMeta.options
        : [];
      if (!raw.length) {
        return [];
      }
      const sorted = [...raw].sort((a, b) => (a?.sort || 0) - (b?.sort || 0));
      return sorted.map((opt, idx) => ({
        key: String(opt?.key ?? idx + 1),
        index: idx + 1,
        text: this.extractOptionText(opt),
      }));
    },
    blankKeys() {
      const blanks = Array.isArray(this.questionMeta?.blanks)
        ? this.questionMeta.blanks
        : [];
      const keys = blanks
        .map((b) => (b && b.key ? String(b.key) : ""))
        .filter(Boolean);
      return keys.length ? keys : ["blank_1"];
    },
    fillTip() {
      if (this.question?.questionType === "SHORT_ANSWER") {
        return "请输入你的答案：";
      }
      if (this.question?.questionType === "FILL_BLANK") {
        return "请输入你认为正确的单词：";
      }
      return "请输入答案：";
    },
    canPrev() {
      return this.session && this.localIndex > 0 && !this.loading;
    },
    canNext() {
      if (!this.session || this.loading) {
        return false;
      }
      const total = this.session.totalCount || 0;
      return this.localIndex < total - 1;
    },
    canSubmit() {
      if (!this.session || !this.question || this.loading) {
        return false;
      }
      if (this.session.finished) {
        return false;
      }
      if (this.isChoice) {
        return this.selectedOptionKeys.length > 0;
      }
      if (this.blankKeys.length > 1) {
        return this.blankKeys.every(
          (k) => this.blankAnswers[k] && `${this.blankAnswers[k]}`.trim()
        );
      }
      return Boolean(this.fillValue && this.fillValue.trim());
    },
  },
  created() {
    const initialNodeId = parsePositiveInt(this.$route.query.nodeId);
    if (initialNodeId) {
      this.nodeIdInput = String(initialNodeId);
    }
    if (this.nodeId) {
      this.startSession();
    }
  },
  watch: {
    "$route.fullPath"() {
      const queryNodeId = parsePositiveInt(this.$route.query.nodeId);
      if (queryNodeId) {
        this.nodeIdInput = String(queryNodeId);
      }
    },
  },
  methods: {
    fallbackTypeLabel(questionType) {
      if (questionType === "SINGLE_CHOICE") return "选择题";
      if (questionType === "MULTIPLE_CHOICE") return "多选题";
      if (questionType === "TRUE_FALSE") return "判断题";
      if (questionType === "FILL_BLANK") return "单词填写题";
      if (questionType === "SHORT_ANSWER") return "简答题";
      return "题目";
    },
    extractOptionText(option) {
      const content = option?.content;
      if (content && typeof content === "object") {
        if (content.text) {
          return String(content.text);
        }
        if (content.html) {
          return String(content.html);
        }
      }
      if (typeof option?.content === "string") {
        return option.content;
      }
      return "";
    },
    applyNodeId() {
      const value = parsePositiveInt(this.nodeIdInput);
      if (!value) {
        window.alert("请输入有效的 nodeId");
        return;
      }
      saveLastLanguageJpPracticeNodeId(value);
      this.$router.replace({
        name: "language-jp-practice",
        params: { parentId: String(this.parentId) },
        query: { nodeId: String(value) },
      });
      this.startSession(value);
    },
    resetAnswerUI() {
      this.selectedOptionKeys = [];
      this.fillValue = "";
      this.blankAnswers = {};
      this.resultBox.visible = false;
      this.resultBox.correct = false;
      this.resultBox.html = "";
    },
    hydrateFromQuestion(question) {
      this.resetAnswerUI();
      if (!question) {
        return;
      }
      const ua = question.userAnswer;
      if (ua && ua.optionKeys && Array.isArray(ua.optionKeys)) {
        this.selectedOptionKeys = ua.optionKeys.map((x) => String(x));
      }
      if (ua && ua.blanks && typeof ua.blanks === "object") {
        this.blankKeys.forEach((k) => {
          if (ua.blanks[k] !== undefined && ua.blanks[k] !== null) {
            this.$set(this.blankAnswers, k, String(ua.blanks[k]));
          }
        });
      }
      if (ua && ua.text) {
        this.fillValue = String(ua.text);
      }
      if (ua && ua.value !== undefined) {
        this.selectedOptionKeys = [String(ua.value)];
      }
      if (question.result) {
        this.showResult(question.result === "CORRECT", null, null);
      }
    },
    isOptionSelected(key) {
      return this.selectedOptionKeys.includes(String(key));
    },
    toggleOption(key) {
      const k = String(key);
      if (this.isMultipleChoice) {
        if (this.isOptionSelected(k)) {
          this.selectedOptionKeys = this.selectedOptionKeys.filter(
            (it) => it !== k
          );
        } else {
          this.selectedOptionKeys = [...this.selectedOptionKeys, k];
        }
        return;
      }
      this.selectedOptionKeys = [k];
    },
    async startSession(forcedNodeId) {
      const useNodeId = parsePositiveInt(forcedNodeId) || this.nodeId;
      if (!useNodeId) {
        return;
      }
      this.loading = true;
      this.error = "";
      this.session = null;
      this.question = null;
      this.localIndex = 0;
      this.resetAnswerUI();
      try {
        const res = await startPracticeSession({
          noteNodeId: useNodeId,
          mode: "SEQUENTIAL",
          size: 20,
        });
        this.session = res?.session || null;
        this.question = res?.question || null;
        this.localIndex = this.session?.currentIndex || 0;
        this.hydrateFromQuestion(this.question);
      } catch (e) {
        this.error = e?.message || "创建会话失败";
      } finally {
        this.loading = false;
      }
    },
    async fetchItem(index) {
      if (!this.session?.sessionId) {
        return;
      }
      this.loading = true;
      this.error = "";
      try {
        const res = await fetchPracticeItem(this.session.sessionId, index);
        this.session = res?.session || this.session;
        this.question = res?.question || null;
        this.localIndex = index;
        this.hydrateFromQuestion(this.question);
      } catch (e) {
        this.error = e?.message || "加载题目失败";
      } finally {
        this.loading = false;
      }
    },
    prev() {
      if (!this.canPrev) return;
      this.fetchItem(this.localIndex - 1);
    },
    next() {
      if (!this.canNext) return;
      this.fetchItem(this.localIndex + 1);
    },
    formatCorrectAnswer(correctAnswer) {
      if (!correctAnswer) return "";
      if (correctAnswer.optionKeys && Array.isArray(correctAnswer.optionKeys)) {
        const keySet = new Set(correctAnswer.optionKeys.map((k) => String(k)));
        const matched = this.normalizedOptions
          .filter((o) => keySet.has(String(o.key)))
          .map((o) => o.text)
          .filter(Boolean);
        if (matched.length) {
          return matched.join(" / ");
        }
        return correctAnswer.optionKeys.join(" / ");
      }
      if (correctAnswer.blanks && typeof correctAnswer.blanks === "object") {
        const parts = Object.entries(correctAnswer.blanks).map(([k, v]) => {
          if (Array.isArray(v)) {
            return `${k}: ${v.join(" / ")}`;
          }
          return `${k}: ${String(v)}`;
        });
        return parts.join("<br>");
      }
      if (correctAnswer.texts && Array.isArray(correctAnswer.texts)) {
        return correctAnswer.texts.join(" / ");
      }
      if (correctAnswer.value !== undefined) {
        return String(correctAnswer.value);
      }
      return JSON.stringify(correctAnswer);
    },
    formatAnalysis(analysis) {
      if (!analysis) return "";
      if (analysis.text) return String(analysis.text);
      if (analysis.html) return String(analysis.html);
      return JSON.stringify(analysis);
    },
    showResult(isCorrect, correctAnswer, analysis) {
      const explanation = this.formatAnalysis(analysis);
      if (isCorrect) {
        this.resultBox.visible = true;
        this.resultBox.correct = true;
        this.resultBox.html = `回答正确。<br>解析：${explanation || "—"}`;
        return;
      }
      const answerText = this.formatCorrectAnswer(correctAnswer);
      this.resultBox.visible = true;
      this.resultBox.correct = false;
      this.resultBox.html = `回答错误。<br>正确答案：${
        answerText || "—"
      }<br>解析：${explanation || "—"}`;
    },
    buildUserAnswer() {
      if (this.isChoice) {
        return {
          optionKeys: this.selectedOptionKeys,
        };
      }
      if (this.question?.questionType === "FILL_BLANK") {
        if (this.blankKeys.length > 1) {
          const blanks = {};
          this.blankKeys.forEach((k) => {
            blanks[k] = this.blankAnswers[k] || "";
          });
          return { blanks };
        }
        return { blanks: { blank_1: this.fillValue } };
      }
      return { text: this.fillValue };
    },
    async submit() {
      if (!this.canSubmit) {
        window.alert("请先填写/选择答案");
        return;
      }
      if (!this.session?.sessionId || !this.question?.questionId) {
        return;
      }
      this.loading = true;
      this.error = "";
      try {
        const payload = {
          index: this.localIndex,
          questionId: this.question.questionId,
          noteNodeId: this.nodeId,
          clientRequestId: randomClientId(),
          userAnswer: this.buildUserAnswer(),
          costMs: null,
          userId: null,
        };
        const res = await submitPractice(this.session.sessionId, payload);
        const isCorrect = res?.result === "CORRECT";
        this.session = res?.session || this.session;
        this.showResult(isCorrect, res?.correctAnswer, res?.analysis);

        if (res?.nextQuestion) {
          this.question = res.nextQuestion;
          this.localIndex = this.session?.currentIndex || this.localIndex + 1;
          this.resetAnswerUI();
        }
      } catch (e) {
        this.error = e?.message || "提交失败";
      } finally {
        this.loading = false;
      }
    },
  },
};
</script>

<style scoped>
* {
  box-sizing: border-box;
}

.page {
  max-width: 860px;
  margin: 0 auto;
  padding: 10px 0 40px;
}

.exam-shell {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.exam-header {
  padding: 18px 20px;
  border-bottom: 1px solid #ececec;
  background: linear-gradient(180deg, #fafafa 0%, #f4f4f4 100%);
}

.exam-title {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 10px;
}

.exam-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 13px;
  color: #666;
}

.meta-tag {
  padding: 4px 10px;
  border-radius: 999px;
  background: #fff;
  border: 1px solid #e6e6e6;
}

.progress-wrap {
  margin-top: 14px;
}

.progress-text {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: #eceff3;
  border-radius: 999px;
  overflow: hidden;
}

.progress-inner {
  height: 100%;
  width: 0;
  background: #3b82f6;
  border-radius: 999px;
  transition: width 0.25s ease;
}

.question-card {
  padding: 22px 20px 18px;
}

.question-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.question-type {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  background: #eef4ff;
  color: #295ec9;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
}

.question-score {
  font-size: 13px;
  color: #777;
}

.question-section {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.question-title {
  font-size: 18px;
  font-weight: 700;
  line-height: 1.7;
  margin-bottom: 14px;
}

.question-desc {
  font-size: 15px;
  line-height: 1.9;
  color: #444;
  background: #fafafa;
  border: 1px solid #ededed;
  border-radius: 10px;
  padding: 14px;
  margin-bottom: 18px;
  white-space: pre-wrap;
}

.listening-box {
  background: #f8fbff;
  border: 1px solid #dbe9ff;
  border-radius: 12px;
  padding: 14px;
  margin-bottom: 18px;
}

.listening-label {
  font-size: 14px;
  font-weight: 600;
  color: #2f5fb8;
  margin-bottom: 10px;
}

audio {
  width: 100%;
}

.options {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 10px;
}

.option-item {
  position: relative;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #fff;
  transition: all 0.2s ease;
  overflow: hidden;
}

.option-item:hover {
  border-color: #c7d2fe;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.08);
}

.option-item input {
  position: absolute;
  opacity: 0;
  pointer-events: none;
}

.option-label {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 14px;
  cursor: pointer;
  line-height: 1.8;
}

.option-index {
  width: 28px;
  height: 28px;
  flex-shrink: 0;
  border-radius: 50%;
  background: #f1f5f9;
  color: #334155;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  margin-top: 1px;
}

.option-text {
  flex: 1;
  font-size: 16px;
  color: #222;
  white-space: pre-wrap;
}

.option-item.active {
  border-color: #3b82f6;
  background: #f8fbff;
}

.option-item.active .option-index {
  background: #3b82f6;
  color: #fff;
}

.fill-box {
  margin-top: 10px;
}

.fill-tip {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.fill-input {
  width: 100%;
  height: 48px;
  border: 1px solid #dcdfe5;
  border-radius: 10px;
  padding: 0 14px;
  font-size: 16px;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
  background: #fff;
  margin-bottom: 10px;
}

.fill-input:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.12);
}

.result-box {
  margin-top: 18px;
  padding: 14px;
  border-radius: 12px;
  display: none;
  line-height: 1.8;
  font-size: 15px;
}

.result-box.show {
  display: block;
}

.result-box.correct {
  background: #ecfdf3;
  border: 1px solid #b7efcc;
  color: #166534;
}

.result-box.wrong {
  background: #fff3f2;
  border: 1px solid #f7c5c0;
  color: #b42318;
}

.exam-footer {
  padding: 16px 20px 20px;
  border-top: 1px solid #ececec;
  background: #fff;
}

.action-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.btn {
  min-width: 110px;
  height: 44px;
  border: none;
  border-radius: 10px;
  padding: 0 18px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-default {
  background: #eef2f7;
  color: #334155;
}

.btn-primary {
  background: #2563eb;
  color: #fff;
}

.btn-success {
  background: #16a34a;
  color: #fff;
}

.summary {
  margin-top: 14px;
  font-size: 14px;
  color: #555;
}

.empty-state {
  padding: 12px 6px;
}

.empty-title {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.7;
  margin-bottom: 12px;
}

.node-input-row {
  display: flex;
  gap: 10px;
}

.node-input {
  flex: 1;
  height: 44px;
  border-radius: 10px;
  border: 1px solid #dcdfe5;
  padding: 0 12px;
  font-size: 15px;
}

.loading,
.error {
  padding: 18px 0;
  text-align: center;
  color: #666;
}

.error {
  color: #ef4444;
}

@media (max-width: 640px) {
  .page {
    padding: 0 0 24px;
  }

  .question-card {
    padding: 18px 14px 14px;
  }

  .exam-header,
  .exam-footer {
    padding-left: 14px;
    padding-right: 14px;
  }

  .question-title {
    font-size: 16px;
  }

  .option-text {
    font-size: 15px;
  }

  .btn {
    flex: 1;
    min-width: 0;
  }
}
</style>
