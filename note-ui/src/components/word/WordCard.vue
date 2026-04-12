<template>
  <div class="word-card" style="text-align: left">
    <div class="learning-top">
      <button
        class="back-to-catalog-btn"
        type="button"
        @click="handleBackToCatalog"
      >
        &lt; 进度 {{ progressText }}
      </button>
      <div class="learning-progress-track">
        <span
          class="learning-progress-fill"
          :style="{ width: `${progressPercent}%` }"
        />
      </div>
      <div class="learning-top-right">
        <label class="playback-rate-label" for="playback-rate-select"
          >速度</label
        >
        <select
          id="playback-rate-select"
          v-model.number="playbackRate"
          class="playback-rate-select"
          @change="savePlaybackRate"
        >
          <option
            v-for="rate in playbackRateOptions"
            :key="`rate-${rate}`"
            :value="rate"
          >
            {{ rate.toFixed(2) }}x
          </option>
        </select>
      </div>
    </div>

    <div class="card" :class="{ 'is-done': isDone }">
      <div class="header">
        <div class="word-row">
          <div class="word-main">
            <span class="done-indicator" aria-hidden="true">✓</span>
            <div class="word-inline">
              <div class="word">{{ wordCard.word.text }}</div>
              <button
                class="inline-audio-btn"
                type="button"
                :disabled="!canPlayWordAudio()"
                title="播放单词"
                aria-label="播放单词"
                @click="playWordAudio"
              >
                <i
                  class="el-icon-video-play inline-audio-icon"
                  aria-hidden="true"
                />
              </button>
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
                <div
                  v-if="wordCard.sections.meaning.meta.kana"
                  class="meaning-meta-item meaning-meta-kana"
                >
                  <span class="label">假名</span>
                  <span>{{ wordCard.sections.meaning.meta.kana }}</span>
                </div>
                <div
                  v-if="wordCard.sections.meaning.meta.zh"
                  class="meaning-meta-item meaning-meta-zh"
                >
                  <span class="label">中文</span>
                  <span>{{ wordCard.sections.meaning.meta.zh }}</span>
                </div>
                <div
                  v-if="wordCard.sections.meaning.meta.romaji"
                  class="meaning-meta-item meaning-meta-romaji"
                >
                  <span class="label">发音</span>
                  <span>{{ wordCard.sections.meaning.meta.romaji }}</span>
                </div>
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
                <div class="sentence-row">
                  <div class="sentence">{{ example.sentence }}</div>
                  <button
                    class="inline-audio-btn inline-audio-btn-example"
                    type="button"
                    :disabled="!canPlayExampleAudio(example)"
                    title="播放例句"
                    aria-label="播放例句"
                    @click="playExampleAudio(example, index)"
                  >
                    <i
                      class="el-icon-video-play inline-audio-icon"
                      aria-hidden="true"
                    />
                  </button>
                </div>
                <div class="example-speech-row">
                  <SpeechTranscribeInput
                    :value="getExampleSpeechText(example, index)"
                    lang="ja-JP"
                    placeholder="点击麦克风，实时转写当前例句"
                    @input="setExampleSpeechText(example, index, $event)"
                    @error="handleExampleSpeechError(index, $event)"
                  />
                </div>
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
                  <div class="example-grid">
                    <div v-if="example.explain.reading" class="example-kv">
                      <span class="label">读音</span
                      >{{ example.explain.reading }}
                    </div>
                    <div v-if="example.explain.romaji" class="example-kv">
                      <span class="label">罗马音</span
                      >{{ example.explain.romaji }}
                    </div>
                    <div v-if="example.explain.meaningZh" class="example-kv">
                      <span class="label">中文</span
                      >{{ example.explain.meaningZh }}
                    </div>
                  </div>
                  <div class="example-subtitle">单词 & 语法拆解</div>
                  <ul class="example-list">
                    <li
                      v-for="(item, itemIndex) in example.explain
                        .wordGrammarBreakdown"
                      :key="`${example.id || index}-${itemIndex}`"
                    >
                      <span class="token token-pronoun">{{
                        formatBreakdownHead(item)
                      }}</span>
                      <span
                        v-if="formatBreakdownDesc(item)"
                        class="token-desc"
                        >{{ formatBreakdownDesc(item) }}</span
                      >
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
                    <div class="example-pattern">
                      <strong>固定句式：</strong
                      >{{ example.explain.fixedPattern.pattern }}
                      <span
                        v-if="example.explain.fixedPattern.meaningZh"
                        class="pattern-sep"
                      >
                        =
                      </span>
                      {{ example.explain.fixedPattern.meaningZh }}
                    </div>
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
          :class="{
            done: action.key === 'done' && isDone,
            hard: action.key === 'hard' && isHard,
            favorite: action.key === 'favorite' && isFavorite,
          }"
          :disabled="action.key === 'done' && confirming"
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
import { confirmWordCardDone, getWordCardPage } from "@/api/wordCards";
import SpeechTranscribeInput from "@/components/speech/SpeechTranscribeInput.vue";
import {
  createDefaultWordCard,
  createMockWordCard,
  normalizeWordCard,
} from "@/model/word/wordCard";
import {
  AudioPlaybackManager,
  loadPlaybackRate,
  savePlaybackRate,
} from "@/utils/audioPlayback";

export default {
  name: "WordCard",
  components: {
    SpeechTranscribeInput,
  },
  // 临时前端配置：播放速度本地存储键。
  // TODO(后端待实现): 若后续要云端同步用户配置，可由接口返回后替换本地读写。
  playbackRateStorageKey: "note-ui.word-card.playback-rate",
  playbackRateOptions: [0.5, 0.75, 1, 1.25, 1.5],
  props: {
    noteId: {
      type: [Number, String],
      required: true,
    },
    index: {
      type: Number,
      default: 0,
    },
    userId: {
      type: [Number, String],
      default: null,
    },
  },
  data() {
    return {
      wordCard: createDefaultWordCard(),
      pageInfo: null,
      loading: false,
      errorMessage: "",
      playbackRate: 1,
      audioPlaybackManager: null,
      requestSeq: 0,
      pageSize: 10,
      confirming: false,
      exampleSpeechTexts: {},
      autoPlaybackSeq: 0,
      autoPlaybackTimers: [],
    };
  },
  computed: {
    safeIndex() {
      const idx = Number(this.index);
      return Number.isFinite(idx) && idx >= 0 ? idx : 0;
    },
    currentPage() {
      return Math.floor(this.safeIndex / this.pageSize) + 1;
    },
    indexInPage() {
      return this.safeIndex - (this.currentPage - 1) * this.pageSize;
    },
    totalCards() {
      const total = Number(this.pageInfo?.total);
      return Number.isFinite(total) && total > 0 ? total : 0;
    },
    currentCardOrder() {
      if (this.totalCards <= 0) {
        return 0;
      }
      return Math.min(this.safeIndex + 1, this.totalCards);
    },
    progressText() {
      return `${this.currentCardOrder}/${this.totalCards}`;
    },
    progressPercent() {
      if (this.totalCards <= 0) {
        return 0;
      }
      const percent = (this.currentCardOrder / this.totalCards) * 100;
      return Math.max(0, Math.min(100, percent));
    },
    isDone() {
      return !!this.wordCard?.progress?.done || !!this.wordCard?.done;
    },
    isHard() {
      return !!this.wordCard?.progress?.hard;
    },
    isFavorite() {
      return !!this.wordCard?.progress?.favorite;
    },
    playbackRateOptions() {
      return this.$options.playbackRateOptions || [1];
    },
  },
  async created() {
    this.initPlaybackRate();
    this.audioPlaybackManager = new AudioPlaybackManager({ lang: "ja-JP" });
    this.audioPlaybackManager.warmupVoices();
    await this.loadWordCard();
  },
  beforeDestroy() {
    this.stopPlayback();
    if (this.audioPlaybackManager) {
      this.audioPlaybackManager.destroy();
      this.audioPlaybackManager = null;
    }
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
    clearAutoPlaybackTimers() {
      const timers = Array.isArray(this.autoPlaybackTimers)
        ? this.autoPlaybackTimers
        : [];
      timers.forEach((timerId) => clearTimeout(timerId));
      this.autoPlaybackTimers = [];
    },
    cancelAutoPlayback() {
      this.autoPlaybackSeq += 1;
      this.clearAutoPlaybackTimers();
    },
    waitWithAutoPlaybackSeq(ms, seq) {
      return new Promise((resolve) => {
        if (seq !== this.autoPlaybackSeq) {
          resolve(false);
          return;
        }
        const timerId = setTimeout(() => {
          const index = this.autoPlaybackTimers.indexOf(timerId);
          if (index >= 0) {
            this.autoPlaybackTimers.splice(index, 1);
          }
          resolve(seq === this.autoPlaybackSeq);
        }, ms);
        this.autoPlaybackTimers.push(timerId);
      });
    },
    async loadWordCard() {
      this.stopPlayback();
      if (
        this.noteId === null ||
        this.noteId === undefined ||
        this.noteId === ""
      ) {
        this.wordCard = createDefaultWordCard();
        this.errorMessage = "缺少 noteId 参数";
        return;
      }

      const seq = ++this.requestSeq;
      this.loading = true;
      this.errorMessage = "";
      try {
        const pageResult = await getWordCardPage({
          noteId: this.noteId,
          page: this.currentPage,
          size: this.pageSize,
          userId: this.userId,
        });
        if (seq !== this.requestSeq) {
          return;
        }
        this.pageInfo = pageResult?.pageInfo || null;
        const records = Array.isArray(pageResult?.records)
          ? pageResult.records
          : [];
        const record = records[this.indexInPage] || records[0] || {};
        this.wordCard = normalizeWordCard(record);
        this.exampleSpeechTexts = {};
        this.tryAutoPlayWordAndExamples();
      } catch (error) {
        if (seq !== this.requestSeq) {
          return;
        }
        this.wordCard = createMockWordCard();
        this.pageInfo = {
          total: 0,
        };
        this.exampleSpeechTexts = {};
        this.errorMessage = `接口请求失败，已回退示例数据：${error.message}`;
      } finally {
        if (seq === this.requestSeq) {
          this.loading = false;
        }
      }
    },
    handleAction(action) {
      if (action?.key === "done") {
        this.handleConfirm();
        return;
      }
      if (action?.key === "hard") {
        this.wordCard.progress.hard = !this.wordCard.progress.hard;
        return;
      }
      if (action?.key === "favorite") {
        this.wordCard.progress.favorite = !this.wordCard.progress.favorite;
        return;
      }
      if (action?.key === "prev" || action?.key === "previous") {
        if (this.safeIndex <= 0) {
          this.errorMessage = "已经是第一张";
          return;
        }
        const currentIndex = this.safeIndex;
        this.$emit("update:index", currentIndex - 1);
        return;
      }
      if (action?.key === "next") {
        if (this.totalCards > 0 && this.safeIndex >= this.totalCards - 1) {
          this.errorMessage = "已经是最后一张";
          return;
        }
        const currentIndex = this.safeIndex;
        this.$emit("update:index", currentIndex + 1);
        return;
      }
      if (action?.key === "audio") {
        this.playWordAndExamplesAudio();
      }
    },
    async handleConfirm() {
      if (this.confirming) {
        return;
      }
      if (!this.wordCard?.id) {
        this.errorMessage = "缺少 cardId，无法确认完成";
        return;
      }

      this.confirming = true;
      this.errorMessage = "";
      try {
        const response = await confirmWordCardDone(
          this.wordCard.id,
          this.userId
        );
        this.wordCard = normalizeWordCard(response || this.wordCard);
      } catch (error) {
        this.errorMessage = `确认完成失败：${error.message}`;
      } finally {
        this.confirming = false;
      }
    },
    canPlayWordAudio() {
      const wordText = this.wordCard?.word?.text || "";
      const wordAudioUrl = this.wordCard?.word?.audioUrl || "";
      return !!wordText || !!wordAudioUrl;
    },
    canPlayExampleAudio(example = {}) {
      const sentenceText = example?.sentence || "";
      const sentenceAudioUrl = example?.audioUrl || "";
      return !!sentenceText || !!sentenceAudioUrl;
    },
    getWordAudioSource() {
      const wordText = this.wordCard?.word?.text || "";
      const wordAudioUrl = this.wordCard?.word?.audioUrl || "";
      if (!wordText && !wordAudioUrl) {
        return null;
      }
      return {
        key: "word",
        text: wordText,
        url: wordAudioUrl,
        rate: this.playbackRate,
      };
    },
    getExampleAudioSource(example = {}, index = 0) {
      const sentenceText = example?.sentence || "";
      const sentenceAudioUrl = example?.audioUrl || "";
      if (!sentenceText && !sentenceAudioUrl) {
        return null;
      }
      return {
        key: `example-${index}`,
        text: sentenceText,
        url: sentenceAudioUrl,
        rate: this.playbackRate,
      };
    },
    initPlaybackRate() {
      this.playbackRate = loadPlaybackRate(
        this.$options.playbackRateStorageKey,
        this.playbackRateOptions
      );
    },
    savePlaybackRate() {
      savePlaybackRate(this.$options.playbackRateStorageKey, this.playbackRate);
    },
    async playWordAudio({ silent = false } = {}) {
      this.cancelAutoPlayback();
      const source = this.getWordAudioSource();
      if (!source) {
        if (!silent) {
          this.errorMessage = "当前单词没有可播放内容";
        }
        return;
      }
      await this.playAudioSequence([source], { silent });
    },
    async playExampleAudio(example = {}, index = 0) {
      this.cancelAutoPlayback();
      const source = this.getExampleAudioSource(example, index);
      if (!source) {
        this.errorMessage = "当前例句没有可播放内容";
        return;
      }
      await this.playAudioSequence([source]);
    },
    async playWordAndExamplesAudio({ silent = false, gapMs = 1500 } = {}) {
      this.cancelAutoPlayback();
      const queue = [];
      const wordSource = this.getWordAudioSource();
      if (!wordSource) {
        if (!silent) {
          this.errorMessage = "缺少单词发音，无法开始整卡朗读";
        }
        return;
      }
      // 整卡朗读固定从卡片单词发音开始，再顺序播放例句。
      queue.push(wordSource);
      const exampleItems = this.wordCard?.sections?.examples?.items || [];
      exampleItems.forEach((example, index) => {
        const source = this.getExampleAudioSource(example, index);
        if (source) {
          queue.push(source);
        }
      });
      if (!queue.length) {
        if (!silent) {
          this.errorMessage = "当前卡片没有可播放内容";
        }
        return;
      }
      await this.playAudioSequenceWithGap(queue, { silent, gapMs });
    },
    async playAudioSequence(sources = [], { silent = false } = {}) {
      if (!Array.isArray(sources) || !sources.length) {
        return;
      }
      if (!this.audioPlaybackManager) {
        if (!silent) {
          this.errorMessage = "播放器初始化中，请稍后重试";
        }
        return;
      }

      this.errorMessage = "";
      try {
        await this.audioPlaybackManager.playSequence(sources);
      } catch (error) {
        if (!silent) {
          this.errorMessage = `播放发音失败：${error.message}`;
        }
      }
    },
    async playAudioSequenceWithGap(
      sources = [],
      { silent = false, gapMs = 2000 } = {}
    ) {
      if (!Array.isArray(sources) || !sources.length) {
        return;
      }
      // 在开始新的“间隔播放”前，先停止现有播放并取消旧的自动播放队列。
      this.stopPlayback();
      const currentSeq = ++this.autoPlaybackSeq;
      for (let index = 0; index < sources.length; index += 1) {
        if (currentSeq !== this.autoPlaybackSeq) {
          return;
        }
        await this.playAudioSequence([sources[index]], { silent });
        if (currentSeq !== this.autoPlaybackSeq) {
          return;
        }
        if (index < sources.length - 1 && gapMs > 0) {
          const stillValid = await this.waitWithAutoPlaybackSeq(
            gapMs,
            currentSeq
          );
          if (!stillValid) {
            return;
          }
        }
      }
    },
    tryAutoPlayWordAndExamples() {
      // 自动播放：单词发音 -> 间隔 2 秒 -> 例句逐条发音（每条之间也间隔 2 秒）。
      this.playWordAndExamplesAudio({ silent: true, gapMs: 2000 });
    },
    stopPlayback() {
      this.cancelAutoPlayback();
      if (this.audioPlaybackManager) {
        this.audioPlaybackManager.stop();
      }
    },
    formatVocabulary(item = {}) {
      const text = item.text || "";
      const kana = item.kana || "";
      if (!text) {
        return "";
      }
      return kana ? `${text}（${kana}）` : text;
    },
    formatBreakdownHead(item = {}) {
      const word = item.word || "";
      const kana = item.kana || "";
      if (!word) {
        return "";
      }
      if (kana && kana !== word) {
        return `${word}(${kana})`;
      }
      return word;
    },
    formatBreakdownDesc(item = {}) {
      const desc = item.desc || "";
      return desc;
    },
    getExampleSpeechKey(example = {}, index = 0) {
      const id = example?.id || `example-${index}`;
      return `speech-${id}-${index}`;
    },
    getExampleSpeechText(example = {}, index = 0) {
      const key = this.getExampleSpeechKey(example, index);
      return this.exampleSpeechTexts[key] || "";
    },
    setExampleSpeechText(example = {}, index = 0, text = "") {
      const key = this.getExampleSpeechKey(example, index);
      this.$set(this.exampleSpeechTexts, key, text || "");
    },
    handleExampleSpeechError(index = 0, errorCode = "") {
      const messageMap = {
        "not-allowed": "麦克风权限被拒绝，请在浏览器设置中允许访问麦克风",
        "no-speech": "没有检测到语音，请靠近麦克风后重试",
        "audio-capture": "无法访问麦克风设备，请检查系统设备权限",
      };
      const prefix = `例句 ${index + 1} 语音转写失败`;
      const detail = messageMap[errorCode] || errorCode || "unknown";
      this.errorMessage = `${prefix}：${detail}`;
    },
    handleBackToCatalog() {
      this.$emit("back");
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
  max-height: calc(100dvh - 16px);
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow: hidden;
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

.learning-top-right {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.playback-rate-label {
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 600;
}

.playback-rate-select {
  height: 24px;
  min-width: 72px;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
  background: #fff;
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 600;
  padding: 0 6px;
}

.card {
  width: 100%;
  height: 100%;
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
  align-items: center;
  gap: 10px;
}

.word-main {
  display: flex;
  align-items: center;
  gap: 8px;
}

.word-inline {
  display: inline-flex;
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

.inline-audio-btn {
  flex: 0 0 auto;
  border: 1px solid #bfdbfe;
  background: #eff6ff;
  color: #1d4ed8;
  border-radius: 999px;
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  cursor: pointer;
}

.inline-audio-btn:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.inline-audio-icon {
  font-size: 14px;
  line-height: 1;
}

.inline-audio-icon::before {
  content: "\25B6";
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
  overflow-y: auto;
  overscroll-behavior: contain;
  -webkit-overflow-scrolling: touch;
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

.fold details[open] .fold-arrow,
.example-explain[open] .fold-arrow {
  transform: rotate(0deg);
}

.fold-content {
  margin-top: 6px;
  padding-left: 18px;
}

.meaning-meta {
  margin-bottom: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.meaning-meta-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  border-radius: 999px;
  border: 1px solid transparent;
  font-size: 12px;
  line-height: 1.4;
  color: #1f2937;
}

.meaning-meta-item .label {
  font-weight: 700;
}

.meaning-meta-kana {
  background: #eff6ff;
  border-color: #dbeafe;
  color: #1e3a8a;
}

.meaning-meta-kana .label {
  color: #1d4ed8;
}

.meaning-meta-zh {
  background: #ecfeff;
  border-color: #cffafe;
  color: #155e75;
}

.meaning-meta-zh .label {
  color: #0e7490;
}

.meaning-meta-romaji {
  background: #f5f3ff;
  border-color: #e9d5ff;
  color: #5b21b6;
}

.meaning-meta-romaji .label {
  color: #7c3aed;
}

.example {
  margin: 0;
  padding-left: 18px;
}

.example li {
  margin-bottom: 3px;
}

.sentence-row {
  display: inline-flex;
  align-items: flex-start;
  gap: 8px;
  max-width: 100%;
}

.sentence {
  font-size: 14px;
  font-weight: 600;
  color: #111;
  line-height: 1.35;
}

.inline-audio-btn-example {
  width: 24px;
  height: 24px;
  margin-top: 1px;
}

.example-note {
  margin-top: 4px;
  font-size: 12px;
  color: #374151;
  line-height: 1.4;
}

.example-speech-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  margin-bottom: 4px;
  max-width: 100%;
}

.example-speech-label {
  font-size: 12px;
  color: #4b5563;
  line-height: 1;
}

.example-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 4px;
  margin-top: 6px;
}

.example-kv {
  font-size: 12px;
  color: #374151;
  line-height: 1.4;
}

.example-kv .label {
  display: inline-block;
  min-width: 72px;
  font-weight: 700;
  color: #111827;
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

.token {
  font-weight: 700;
  padding: 0 4px;
  border-radius: 4px;
  margin-right: 4px;
  display: inline-block;
}

.token-pronoun {
  color: #1d4ed8;
  background: #dbeafe;
}

.token-desc {
  color: #374151;
}

.example-pattern {
  margin-top: 8px;
  padding: 6px 8px;
  border-radius: 8px;
  background: #eff6ff;
  border: 1px solid #dbeafe;
  font-size: 12px;
  color: #1e3a8a;
  line-height: 1.4;
}

.pattern-sep {
  margin: 0 4px;
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

.icon-btn.hard {
  background: #fff7ed;
  color: #c2410c;
  border-color: #fed7aa;
}

.icon-btn.favorite {
  background: #fefce8;
  color: #a16207;
  border-color: #fde68a;
}

.icon-btn:active {
  transform: scale(0.98);
  background: #eef3ff;
}

.icon-btn:disabled {
  cursor: not-allowed;
  opacity: 0.65;
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
    gap: 12px;
  }

  .card {
    min-height: 0;
  }

  .body {
    padding: 18px 18px 14px;
  }
}
</style>
