<template>
  <div class="word-card" style="text-align: left">
    <div class="learning-top">
      <button class="back-to-catalog-btn" type="button">&lt; 进度 3/20</button>
      <div class="learning-progress-track">
        <span class="learning-progress-fill" />
      </div>
    </div>

    <div class="card" :class="{ 'is-done': wordCard.done }">
      <div class="header">
        <div class="word-row">
          <div class="word-main">
            <span class="done-indicator" aria-hidden="true">✓</span>
            <div>
              <div class="word">{{ wordCard.word.text }}</div>
            </div>
          </div>
        </div>

        <div class="tags">
          <span
            v-for="(tag, tagIndex) in wordCard.tags"
            :key="`tag-${tagIndex}-${tag.name}`"
            class="tag"
            :class="tag.className"
          >
            {{ tag.name }}
          </span>
        </div>

        <div v-if="loading" class="status-text">正在加载单词卡片...</div>
        <div v-else-if="errorMessage" class="status-text error">
          {{ errorMessage }}
        </div>
      </div>

      <div class="body">
        <div class="section fold">
          <details :open="!wordCard.sections.meaning.collapsedByDefault">
            <summary>
              <div class="title fold-summary">
                <span class="fold-arrow">▼</span>
                <span class="title-jp">意味</span>
                <span class="title-cn">解释</span>
              </div>
            </summary>
            <div class="content fold-content">
              <div class="meaning-meta">
                假名：{{ wordCard.sections.meaning.meta.kana }} 中文：{{
                  wordCard.sections.meaning.meta.zh
                }}
                发音：{{ wordCard.sections.meaning.meta.romaji }}
              </div>
              {{ wordCard.sections.meaning.description }}
            </div>
          </details>
        </div>

        <div class="section">
          <div class="title">
            <span class="title-jp">例文</span>
            <span class="title-cn">例句</span>
          </div>
          <div class="fold-content">
            <ol class="example">
              <li
                v-for="(example, index) in wordCard.sections.examples.items"
                :key="example.id || `example-${index}`"
              >
                <div class="sentence">{{ example.sentence }}</div>
                <details
                  class="example-explain"
                  :open="!example.explain.collapsedByDefault"
                >
                  <summary>
                    <div class="fold-summary">
                      <span class="fold-arrow">▼</span>
                      <span>例句解释</span>
                    </div>
                  </summary>
                  <div class="example-note">
                    <strong>读音：</strong>{{ example.explain.reading }}
                  </div>
                  <div class="example-note">({{ example.explain.romaji }})</div>
                  <div class="example-note">
                    <strong>中文意思：</strong>{{ example.explain.meaningZh }}
                  </div>
                  <div class="example-subtitle">单词 & 语法拆解</div>
                  <ul class="example-list">
                    <li
                      v-for="(item, itemIndex) in example.explain
                        .wordGrammarBreakdown"
                      :key="`${example.id || index}-${itemIndex}`"
                    >
                      {{ formatWordGrammarBreakdown(item) }}
                    </li>
                  </ul>
                  <div
                    v-if="
                      example.explain.fixedPattern &&
                      (example.explain.fixedPattern.pattern ||
                        example.explain.fixedPattern.meaningZh)
                    "
                    class="example-note"
                  >
                    <strong>固定句式：</strong
                    >{{ example.explain.fixedPattern.pattern }} =
                    {{ example.explain.fixedPattern.meaningZh }}
                  </div>
                </details>
              </li>
            </ol>
          </div>
        </div>

        <div class="section">
          <div class="title">
            <span class="title-jp">類義語</span>
            <span class="title-cn">相似词</span>
          </div>
          <div>
            <span
              v-for="(item, index) in wordCard.sections.synonyms.items"
              :key="`synonym-${index}-${item.text}`"
              class="word-box"
            >
              {{ formatVocabulary(item) }}
            </span>
          </div>
        </div>

        <div class="section">
          <div class="title">
            <span class="title-jp">関連語</span>
            <span class="title-cn">关联词</span>
          </div>
          <div>
            <span
              v-for="(item, index) in wordCard.sections.related.items"
              :key="`related-${index}-${item.text}`"
              class="word-box"
            >
              {{ formatVocabulary(item) }}
            </span>
          </div>
        </div>
      </div>

      <div class="actions">
        <button
          v-for="action in wordCard.actions"
          :key="action.key"
          class="icon-btn"
          :class="{ done: action.key === 'done' && wordCard.done }"
          :title="action.title"
          @click="handleAction(action)"
        >
          {{ action.icon }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { getWordCardByNoteAndIndex } from "@/api/wordCards";
import {
  createDefaultWordCard,
  createMockWordCard,
  normalizeWordCard,
} from "@/model/word/wordCard";
import privateAudioSrc from "@/assets/私.mp3";

export default {
  name: "WordCard",
  props: {
    noteId: {
      type: [Number, String],
      required: true,
    },
    index: {
      type: Number,
      default: 0,
    },
  },
  data() {
    return {
      wordCard: createDefaultWordCard(),
      loading: false,
      errorMessage: "",
      pronunciationAudio: null,
    };
  },
  async created() {
    await this.loadWordCard();
  },
  beforeDestroy() {
    this.destroyPronunciationAudio();
  },
  watch: {
    noteId() {
      this.loadWordCard();
    },
    index() {
      this.loadWordCard();
    },
  },
  methods: {
    async loadWordCard() {
      if (
        this.noteId === null ||
        this.noteId === undefined ||
        this.noteId === ""
      ) {
        this.wordCard = createDefaultWordCard();
        this.errorMessage = "缺少 noteId 参数";
        return;
      }

      this.loading = true;
      this.errorMessage = "";
      try {
        const response = await getWordCardByNoteAndIndex(
          this.noteId,
          this.index
        );
        this.wordCard = normalizeWordCard(response || {});
      } catch (error) {
        this.wordCard = createMockWordCard();
        this.errorMessage = `接口请求失败，已回退示例数据：${error.message}`;
      } finally {
        this.loading = false;
      }
    },
    handleAction(action) {
      if (action?.key === "done") {
        this.wordCard.done = !this.wordCard.done;
        return;
      }
      if (action?.key === "next") {
        const currentIndex = Number(this.index) || 0;
        this.$emit("update:index", currentIndex + 1);
        return;
      }
      if (action?.key === "audio") {
        this.playPronunciation();
      }
    },
    ensurePronunciationAudio() {
      if (!this.pronunciationAudio) {
        this.pronunciationAudio = new Audio(privateAudioSrc);
      }
      return this.pronunciationAudio;
    },
    playPronunciation() {
      try {
        const audio = this.ensurePronunciationAudio();
        audio.pause();
        audio.currentTime = 0;
        const playPromise = audio.play();
        if (playPromise && typeof playPromise.catch === "function") {
          playPromise.catch((error) => {
            this.errorMessage = `播放发音失败：${error.message}`;
          });
        }
      } catch (error) {
        this.errorMessage = `播放发音失败：${error.message}`;
      }
    },
    destroyPronunciationAudio() {
      if (!this.pronunciationAudio) {
        return;
      }
      this.pronunciationAudio.pause();
      this.pronunciationAudio.currentTime = 0;
      this.pronunciationAudio = null;
    },
    formatVocabulary(item = {}) {
      const text = item.text || "";
      const kana = item.kana || "";
      if (!text) {
        return "";
      }
      return kana ? `${text}（${kana}）` : text;
    },
    formatWordGrammarBreakdown(item = {}) {
      const word = item.word || "";
      const kana = item.kana || "";
      const desc = item.desc || "";
      if (!word) {
        return desc;
      }
      const head = kana ? `${word}(${kana})` : word;
      return desc ? `${head}: ${desc}` : head;
    },
  },
};
</script>

<style scoped>
* {
  box-sizing: border-box;
}

.word-card {
  width: min(100%, 420px);
  text-align: left;
  height: calc(100dvh - 16px);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.learning-top {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid #dbe4ff;
  background: linear-gradient(135deg, #f8fbff, #eef4ff);
  white-space: nowrap;
}

.back-to-catalog-btn {
  flex: 0 0 auto;
  border: none;
  background: transparent;
  color: #1d4ed8;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  padding: 0;
  text-align: left;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.learning-progress-track {
  flex: 1;
  min-width: 80px;
  height: 7px;
  border-radius: 999px;
  background: #dbe4ff;
  overflow: hidden;
}

.learning-progress-fill {
  display: block;
  width: 15%;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #2563eb, #60a5fa);
}

.card {
  width: 100%;
  height: auto;
  flex: 1;
  min-height: 0;
  margin: 0;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  text-align: left;
}

.header {
  padding: 14px 14px 12px;
  background: linear-gradient(135deg, #eef4ff, #f8fbff);
  border-bottom: 1px solid #eee;
}

.word-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.word-main {
  display: flex;
  align-items: center;
  gap: 8px;
}

.done-indicator {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  border: 1px solid #c7d2fe;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  color: transparent;
  background: #eff4ff;
  transition: all 0.2s ease;
}

.card.is-done .done-indicator {
  color: #fff;
  background: #3b82f6;
  border-color: #3b82f6;
}

.word {
  font-size: clamp(28px, 8vw, 34px);
  font-weight: 700;
  color: #2563eb;
}

.tags {
  margin-top: 10px;
}

.tag {
  display: inline-block;
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 999px;
  margin: 0 4px 4px 0;
  font-weight: 500;
}

.tag-n5 {
  background: #e0f2fe;
  color: #0369a1;
}

.tag-n4 {
  background: #ede9fe;
  color: #6d28d9;
}

.tag-pos {
  background: #dcfce7;
  color: #166534;
}

.tag-type {
  background: #fff7ed;
  color: #c2410c;
}

.tag-core {
  background: #fee2e2;
  color: #b91c1c;
}

.body {
  flex: 1;
  min-height: 0;
  padding: 12px 14px 10px;
  overflow: hidden;
  text-align: left;
}

.section {
  margin-top: 10px;
}

.title {
  margin-bottom: 4px;
}

.title-jp {
  font-size: 14px;
  font-weight: 700;
  color: #2563eb;
}

.title-cn {
  font-size: 12px;
  color: #999;
  margin-left: 6px;
}

.content {
  font-size: 13px;
  color: #374151;
  line-height: 1.35;
}

.fold {
  margin-top: 10px;
}

.fold summary {
  list-style: none;
  cursor: pointer;
  user-select: none;
}

.fold summary::-webkit-details-marker {
  display: none;
}

.fold-summary {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.fold-arrow {
  font-size: 12px;
  color: #6b7280;
  transform: rotate(-90deg);
  transition: transform 0.2s ease;
}

.fold details[open] .fold-arrow {
  transform: rotate(0deg);
}

.fold-content {
  margin-top: 6px;
  padding-left: 18px;
}

.meaning-meta {
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #111827;
  line-height: 1.35;
}

.example {
  margin: 0;
  padding-left: 18px;
}

.example li {
  margin-bottom: 3px;
}

.sentence {
  font-size: 14px;
  font-weight: 600;
  color: #111;
  line-height: 1.35;
}

.example-note {
  margin-top: 4px;
  font-size: 12px;
  color: #374151;
  line-height: 1.4;
}

.example-subtitle {
  margin-top: 8px;
  font-size: 12px;
  font-weight: 700;
  color: #111827;
}

.example-list {
  margin: 4px 0 0 16px;
  padding: 0;
  font-size: 12px;
  color: #374151;
  line-height: 1.45;
}

.example-list li {
  margin-bottom: 2px;
}

.example-explain {
  margin-top: 4px;
}

.example-explain summary {
  list-style: none;
  cursor: pointer;
  user-select: none;
  color: #2563eb;
  font-size: 12px;
  font-weight: 600;
}

.example-explain summary::-webkit-details-marker {
  display: none;
}

.example-explain .fold-summary {
  margin-bottom: 0;
}

.word-box {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 10px;
  font-size: 12px;
  margin: 2px 3px 2px 0;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
}

.actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  padding: 8px 10px calc(8px + env(safe-area-inset-bottom));
  border-top: 1px solid #eceff5;
  background: #fff;
}

.icon-btn {
  flex: 1;
  height: 42px;
  border: 1px solid #e3e7ef;
  border-radius: 12px;
  background: #f8faff;
  color: #1f2937;
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
}

.icon-btn.done {
  background: #3b82f6;
  color: #fff;
  border-color: #3b82f6;
}

.icon-btn:active {
  transform: scale(0.98);
  background: #eef3ff;
}

.status-text {
  margin-top: 10px;
  font-size: 12px;
  color: #4b5563;
}

.status-text.error {
  color: #b91c1c;
}

@media (min-width: 640px) {
  .word-card {
    height: auto;
    gap: 12px;
  }

  .card {
    height: auto;
    min-height: 780px;
  }

  .body {
    overflow: visible;
    padding: 18px 18px 14px;
  }
}
</style>
