<template>
  <div class="article-reader-page">
    <div class="reader-shell">
      <div class="header">
        <button class="header-btn" type="button" @click="goBack">返回</button>
        <div class="header-title">文章阅读</div>
        <div class="header-placeholder" aria-hidden="true" />
      </div>

      <div class="content" ref="content">
        <div class="article-card">
          <div v-if="loading" class="status-text">加载中...</div>
          <div v-else-if="errorMessage" class="status-text error">
            {{ errorMessage }}
          </div>
          <template v-else>
            <div class="article-meta">
              <span
                v-for="tag in articleMetaTags"
                :key="`article-tag-${tag}`"
                class="tag"
              >
                {{ tag }}
              </span>
            </div>
            <h1 class="article-title">{{ article.title }}</h1>
            <div class="section">
              <div
                class="jp-article-block"
                :class="{ 'hidden-rt': !rubyVisible }"
              >
                <p class="jp-paragraph">
                  <template
                    v-for="(paragraph, paragraphIndex) in article.paragraphs"
                  >
                    <span
                      :key="`paragraph-${paragraphIndex}`"
                      :ref="`paragraph-${paragraphIndex}`"
                      class="paragraph-inline"
                      :class="{
                        active: paragraphIndex === currentParagraphIndex,
                      }"
                      @click="selectParagraph(paragraphIndex)"
                    >
                      <template v-for="(token, tokenIndex) in paragraph">
                        <ruby
                          v-if="token.kana"
                          :key="`ruby-${paragraphIndex}-${tokenIndex}`"
                        >
                          {{ token.text }}<rt>{{ token.kana }}</rt>
                        </ruby>
                        <span
                          v-else
                          :key="`text-${paragraphIndex}-${tokenIndex}`"
                        >
                          {{ token.text }}
                        </span>
                      </template>
                    </span>
                  </template>
                </p>
              </div>

              <div v-if="hasTranslations" class="translation-card">
                <div class="translation">
                  <template v-for="(line, lineIndex) in normalizedTranslations">
                    <span :key="`translation-${lineIndex}`">{{ line }}</span>
                    <br
                      v-if="lineIndex !== normalizedTranslations.length - 1"
                      :key="`translation-break-${lineIndex}`"
                    />
                  </template>
                </div>
              </div>
            </div>

            <div
              v-if="coreVocabulary.length || coreSentencePatterns.length"
              class="knowledge-card"
              aria-label="核心知识点"
            >
              <h2 class="knowledge-title">核心知识点</h2>
              <div class="knowledge-content">
                <details
                  v-if="coreVocabulary.length"
                  class="knowledge-block"
                  open
                >
                  <summary class="knowledge-subtitle">核心单词</summary>
                  <ul class="knowledge-list">
                    <li
                      v-for="item in coreVocabulary"
                      :key="`word-${item.jp}-${item.kana}`"
                      class="knowledge-item"
                    >
                      <span class="jp">{{ item.jp }}</span>
                      <span class="meaning">：{{ item.meaning }}</span>
                    </li>
                  </ul>
                </details>

                <details
                  v-if="coreSentencePatterns.length"
                  class="knowledge-block"
                  open
                >
                  <summary class="knowledge-subtitle">核心句式</summary>
                  <ul class="knowledge-list">
                    <li
                      v-for="pattern in coreSentencePatterns"
                      :key="`pattern-${pattern.jp}`"
                      class="knowledge-item"
                    >
                      <span class="jp">{{ pattern.jp }}</span>
                      <span class="meaning">：{{ pattern.meaning }}</span>
                    </li>
                  </ul>
                </details>
              </div>
            </div>
          </template>
        </div>
      </div>

      <div class="toolbar">
        <button
          class="tool-btn secondary icon"
          :class="{ 'is-off': !rubyVisible }"
          type="button"
          :title="rubyVisible ? '隐藏平假名' : '显示平假名'"
          :aria-label="rubyVisible ? '隐藏平假名' : '显示平假名'"
          @click="toggleRuby"
        >
          {{ rubyVisible ? "あ" : "ア" }}
        </button>

        <button
          class="tool-btn secondary icon"
          type="button"
          title="收藏"
          aria-label="收藏"
          @click="toggleFavorite"
        >
          {{ isFavorite ? "★" : "☆" }}
        </button>

        <button
          class="tool-btn icon"
          type="button"
          title="播放全文"
          aria-label="播放全文"
          :disabled="loading"
          @click="playAll"
        >
          ▶
        </button>

        <button
          class="tool-btn secondary icon"
          type="button"
          title="上一段"
          aria-label="上一段"
          :disabled="currentParagraphIndex <= 0"
          @click="goPrev"
        >
          ◀
        </button>

        <select
          v-model.number="playbackRate"
          class="speed-select"
          aria-label="播放速度"
          @change="changePlaybackRate"
        >
          <option
            v-for="rate in playbackRateOptions"
            :key="`rate-${rate}`"
            :value="rate"
          >
            {{ formatPlaybackRateLabel(rate) }}x
          </option>
        </select>

        <button
          class="tool-btn icon"
          type="button"
          title="下一段"
          aria-label="下一段"
          :disabled="currentParagraphIndex >= maxParagraphIndex"
          @click="goNext"
        >
          ▶
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { createDefaultArticleVm } from "@/mappers/domain/language-jp/article.mapper";
import {
  fetchArticleByNoteNode,
  saveArticleFavorite,
  saveArticlePlaybackRate,
  saveArticlePosition,
} from "@/views/domain/language-jp/services/article.service";
import { AudioPlaybackManager } from "@/utils/audioPlayback";

export default {
  name: "ArticleReader",
  props: {
    noteNodeId: {
      type: Number,
      required: true,
    },
  },
  data() {
    return {
      loading: false,
      errorMessage: "",
      article: createDefaultArticleVm(),
      rubyVisible: true,
      currentParagraphIndex: 0,
      playbackRate: 1.0,
      playbackRateOptions: [0.5, 0.75, 1.0, 1.25, 1.5],
      audioManager: new AudioPlaybackManager({ lang: "ja-JP" }),
    };
  },
  computed: {
    isFavorite() {
      return Boolean(this.article.progress && this.article.progress.favorite);
    },
    paragraphCount() {
      return Array.isArray(this.article.paragraphs)
        ? this.article.paragraphs.length
        : 0;
    },
    maxParagraphIndex() {
      return Math.max(
        0,
        ((this.article.paragraphs && this.article.paragraphs.length) || 1) - 1
      );
    },
    articleMetaTags() {
      const tags = ["日语文章", "振假名"];
      const levelTag =
        this.paragraphCount >= 6
          ? "进阶阅读"
          : this.paragraphCount >= 3
          ? "标准阅读"
          : "轻量阅读";
      tags.push(levelTag);
      return tags;
    },
    normalizedTranslations() {
      return Array.isArray(this.article.translation)
        ? this.article.translation
            .map((line) => `${line || ""}`.trim())
            .filter((line) => line)
        : [];
    },
    hasTranslations() {
      return this.normalizedTranslations.length > 0;
    },
    coreVocabulary() {
      const list = [];
      const seen = new Set();
      const paragraphs = Array.isArray(this.article.paragraphs)
        ? this.article.paragraphs
        : [];

      for (let i = 0; i < paragraphs.length; i += 1) {
        const paragraph = Array.isArray(paragraphs[i]) ? paragraphs[i] : [];
        for (let j = 0; j < paragraph.length; j += 1) {
          const token = paragraph[j] || {};
          const text = `${token.text || ""}`.trim();
          const kana = `${token.kana || ""}`.trim();
          if (!text || !kana) {
            continue;
          }
          const dedupKey = `${text}__${kana}`;
          if (seen.has(dedupKey)) {
            continue;
          }
          seen.add(dedupKey);
          list.push({
            jp: `${text}（${kana}）`,
            kana,
            meaning: "读音提示",
          });
          if (list.length >= 8) {
            return list;
          }
        }
      }
      return list;
    },
    coreSentencePatterns() {
      const joined = [];
      for (let i = 0; i < this.paragraphCount; i += 1) {
        joined.push(this.paragraphText(i));
      }
      const text = joined.join("\n");
      const patterns = [];
      const appendPattern = (jp, meaning) => {
        if (patterns.some((item) => item.jp === jp)) {
          return;
        }
        patterns.push({ jp, meaning });
      };

      if (/て、|て。|て，/u.test(text)) {
        appendPattern("〜を 〜て、〜ます", "用て形连接动作，表示先后顺序。");
      }
      if (/で/u.test(text) && /を/u.test(text) && /(ます|です)/u.test(text)) {
        appendPattern("〜で 〜を 〜します", "表示“在某地做某事”。");
      }
      if (/に/u.test(text) && /(ます|です)/u.test(text)) {
        appendPattern("〜は 〜に 〜ます", "表示“在某时间点做某事”。");
      }

      if (!patterns.length) {
        appendPattern("〜は 〜です/ます", "基础叙述句型，表达状态或动作。");
      }
      return patterns.slice(0, 3);
    },
  },
  watch: {
    noteNodeId: {
      immediate: true,
      handler() {
        this.loadArticle();
      },
    },
  },
  mounted() {
    this.audioManager.warmupVoices();
  },
  beforeDestroy() {
    this.audioManager.stop();
    this.audioManager.destroy();
  },
  methods: {
    async loadArticle() {
      this.loading = true;
      this.errorMessage = "";
      try {
        this.article = await fetchArticleByNoteNode(this.noteNodeId);
        this.playbackRate = this.pickPlaybackRate(
          this.article.progress && this.article.progress.playbackRate
        );

        const initIndex =
          (this.article.progress &&
            this.article.progress.lastReadParagraphIndex) ||
          0;
        this.currentParagraphIndex = this.limitIndex(initIndex);
        this.$nextTick(() =>
          this.scrollToParagraph(this.currentParagraphIndex)
        );
      } catch (error) {
        console.error("[article-reader] loadArticle failed:", error);
        this.errorMessage = "文章加载失败";
      } finally {
        this.loading = false;
      }
    },
    pickPlaybackRate(rate) {
      const parsed = Number(rate);
      if (this.playbackRateOptions.includes(parsed)) {
        return parsed;
      }
      return 1.0;
    },
    formatPlaybackRateLabel(rate) {
      const parsed = Number(rate);
      if (!Number.isFinite(parsed) || parsed <= 0) {
        return "1.0";
      }
      const fixed = parsed.toFixed(2);
      return fixed.endsWith(".00") ? fixed.slice(0, -1) : fixed;
    },
    limitIndex(index) {
      const parsed = Number(index);
      if (!Number.isInteger(parsed) || parsed < 0) {
        return 0;
      }
      return Math.min(parsed, this.maxParagraphIndex);
    },
    paragraphText(index) {
      const paragraph = this.article.paragraphs[index] || [];
      return paragraph.map((token) => token.text || "").join("");
    },
    async playCurrent() {
      const text = this.paragraphText(this.currentParagraphIndex);
      if (!text) {
        return;
      }
      try {
        await this.audioManager.playSequence([
          { text, rate: this.playbackRate },
        ]);
      } catch (error) {
        console.error("[article-reader] playCurrent failed:", error);
      }
    },
    async playAll() {
      const sequence = [];
      for (let i = 0; i < this.paragraphCount; i += 1) {
        const text = this.paragraphText(i);
        if (!text) {
          continue;
        }
        sequence.push({ text, rate: this.playbackRate });
      }
      if (!sequence.length) {
        return;
      }
      try {
        await this.audioManager.playSequence(sequence);
      } catch (error) {
        console.error("[article-reader] playAll failed:", error);
      }
    },
    toggleRuby() {
      this.rubyVisible = !this.rubyVisible;
    },
    async toggleFavorite() {
      if (!this.article.id) {
        return;
      }
      try {
        this.article = await saveArticleFavorite(
          this.article,
          !this.isFavorite
        );
      } catch (error) {
        console.error("[article-reader] toggleFavorite failed:", error);
      }
    },
    async changePlaybackRate() {
      if (!this.article.id) {
        return;
      }
      const nextRate = this.pickPlaybackRate(this.playbackRate);
      this.playbackRate = nextRate;
      try {
        this.article = await saveArticlePlaybackRate(this.article, nextRate);
      } catch (error) {
        console.error("[article-reader] changePlaybackRate failed:", error);
      }
    },
    async selectParagraph(index) {
      const nextIndex = this.limitIndex(index);
      this.currentParagraphIndex = nextIndex;
      this.scrollToParagraph(nextIndex);
      await this.persistPosition(nextIndex);
    },
    async goPrev() {
      if (this.currentParagraphIndex <= 0) {
        return;
      }
      await this.selectParagraph(this.currentParagraphIndex - 1);
    },
    async goNext() {
      if (this.currentParagraphIndex >= this.maxParagraphIndex) {
        return;
      }
      await this.selectParagraph(this.currentParagraphIndex + 1);
    },
    async persistPosition(index) {
      if (!this.article.id) {
        return;
      }
      try {
        this.article = await saveArticlePosition(this.article, index);
      } catch (error) {
        console.error("[article-reader] persistPosition failed:", error);
      }
    },
    scrollToParagraph(index) {
      const refKey = `paragraph-${index}`;
      const target = this.$refs[refKey];
      const el = Array.isArray(target) ? target[0] : target;
      if (!el || typeof el.scrollIntoView !== "function") {
        return;
      }
      el.scrollIntoView({ block: "center", behavior: "smooth" });
    },
    goBack() {
      this.$emit("back");
    },
  },
};
</script>

<style scoped>
.article-reader-page {
  min-height: 100dvh;
  background: #f6f3ee;
  font-family: -apple-system, BlinkMacSystemFont, "Hiragino Mincho ProN",
    "Yu Mincho", "Noto Serif JP", "PingFang SC", serif;
}

.reader-shell {
  max-width: 480px;
  margin: 0 auto;
  height: 100dvh;
  background: #fbf8f3;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: rgba(251, 248, 243, 0.94);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid #e8e0d5;
}

.header-btn {
  border: none;
  background: #efe7db;
  color: #5a4632;
  border-radius: 10px;
  padding: 8px 12px;
  font-size: 13px;
  cursor: pointer;
}

.header-title {
  font-size: 16px;
  font-weight: 700;
  color: #3d2f22;
}

.header-placeholder {
  width: 50px;
  height: 33px;
}

.content {
  padding: 18px 16px;
  flex: 1;
  overflow-y: auto;
  overscroll-behavior: contain;
}

.article-card {
  background: #fffdf9;
  border: 1px solid #eee4d7;
  border-radius: 18px;
  padding: 18px 16px;
  box-shadow: 0 6px 18px rgba(80, 55, 20, 0.04);
}

.article-title {
  margin: 0;
  font-size: 22px;
  line-height: 1.5;
  color: #2d241b;
  font-weight: 700;
  text-align: center;
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.tag {
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  color: #7a5b3d;
  background: #f4eadc;
}

.section {
  margin-top: 18px;
}

.jp-article-block {
  background: #fffaf3;
  border-radius: 14px;
  border: 1px solid #f0e5d6;
  padding: 16px 14px;
}

.jp-paragraph {
  margin: 0;
  font-size: 22px;
  line-height: 2.3;
  letter-spacing: 0.02em;
  color: #1f1a14;
  word-break: break-word;
}

.paragraph-inline {
  transition: background-color 0.2s ease;
  border-radius: 8px;
  cursor: pointer;
}

.paragraph-inline.active {
  background: rgba(200, 173, 141, 0.24);
}

ruby {
  ruby-position: over;
}

rt {
  font-size: 0.48em;
  color: #9c7d5e;
  letter-spacing: 0;
}

.hidden-rt rt {
  display: none;
}

.translation {
  margin-top: 0;
  padding: 0;
  color: #6a5746;
  font-size: 14px;
  line-height: 1.9;
}

.translation-card {
  margin-top: 12px;
  padding: 12px 14px;
  background: #f8f3ec;
  border-radius: 12px;
}

.translation {
  color: #6a5746;
  font-size: 14px;
  line-height: 1.9;
}

.knowledge-card {
  margin-top: 16px;
  background: #fffcf7;
  border: 1px solid #eadfcf;
  border-radius: 16px;
  padding: 14px;
}

.knowledge-title {
  margin: 0;
  color: #5c4732;
  font-weight: 700;
  font-size: 15px;
}

.knowledge-content {
  margin-top: 12px;
}

.knowledge-block + .knowledge-block {
  margin-top: 12px;
}

.knowledge-subtitle {
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  list-style: none;
  margin: 0;
  font-size: 13px;
  color: #7a5b3d;
  font-weight: 700;
}

.knowledge-subtitle::-webkit-details-marker {
  display: none;
}

.knowledge-subtitle::after {
  content: "▾";
  font-size: 11px;
  color: #9a7b5d;
  transform: rotate(-90deg);
  transition: transform 0.2s ease;
}

details[open] > .knowledge-subtitle::after {
  transform: rotate(0deg);
}

.knowledge-list {
  margin: 8px 0 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 8px;
}

.knowledge-item {
  padding: 10px 12px;
  border-radius: 10px;
  background: #fff;
  border: 1px solid #f0e5d6;
  font-size: 13px;
  line-height: 1.8;
  color: #4c3d31;
}

.knowledge-item .jp {
  color: #2d241b;
  font-weight: 700;
}

.knowledge-item .meaning {
  color: #7a6654;
}

.toolbar {
  display: flex;
  gap: 10px;
  flex-wrap: nowrap;
  align-items: center;
  padding: 12px 16px;
  border-top: 1px solid #e8e0d5;
  background: rgba(251, 248, 243, 0.96);
  backdrop-filter: blur(8px);
  overflow: hidden;
}

.tool-btn {
  flex: 1 1 0;
  border: none;
  border-radius: 12px;
  padding: 12px 14px;
  background: #5f4630;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;
}

.tool-btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.tool-btn.icon {
  min-width: 0;
  padding: 12px 10px;
  font-size: 16px;
  line-height: 1;
}

.tool-btn.icon.is-off {
  background: #efe7db;
  color: #5a4632;
  border: 1px solid #dfcfbb;
}

.tool-btn.secondary {
  background: #efe7db;
  color: #5a4632;
}

.speed-select {
  flex: 1.45 1 0;
  border: 1px solid #e0d5c6;
  border-radius: 12px;
  background: #fffdf9;
  color: #5a4632;
  padding: 0 10px;
  height: 40px;
  font-size: 13px;
  min-width: 0;
}

.status-text {
  padding: 28px 8px;
  color: #5a4632;
  text-align: center;
}

.status-text.error {
  color: #b42318;
}

@media (max-width: 390px) {
  .jp-paragraph {
    font-size: 20px;
    line-height: 2.2;
  }

  .tool-btn {
    font-size: 12px;
    padding: 10px 8px;
  }

  .tool-btn.icon {
    min-width: 40px;
    padding: 10px 8px;
  }
}
</style>
