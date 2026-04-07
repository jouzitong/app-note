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
            <h1 class="article-title">{{ article.title }}</h1>
            <div class="section">
              <div
                v-for="(paragraph, paragraphIndex) in article.paragraphs"
                :key="`paragraph-${paragraphIndex}`"
                :ref="`paragraph-${paragraphIndex}`"
                class="paragraph-block"
                :class="{ active: paragraphIndex === currentParagraphIndex }"
                @click="selectParagraph(paragraphIndex)"
              >
                <p class="jp-paragraph" :class="{ 'hidden-rt': !rubyVisible }">
                  <template v-for="(token, tokenIndex) in paragraph">
                    <ruby
                      v-if="token.kana"
                      :key="`ruby-${paragraphIndex}-${tokenIndex}`"
                    >
                      {{ token.text }}<rt>{{ token.kana }}</rt>
                    </ruby>
                    <span v-else :key="`text-${paragraphIndex}-${tokenIndex}`">
                      {{ token.text }}
                    </span>
                  </template>
                </p>

                <div
                  v-if="article.translation[paragraphIndex]"
                  class="translation"
                >
                  {{ article.translation[paragraphIndex] }}
                </div>
              </div>
            </div>
          </template>
        </div>
      </div>

      <div class="toolbar">
        <button class="tool-btn secondary" type="button" @click="toggleRuby">
          {{ rubyVisible ? "隐藏平假名" : "显示平假名" }}
        </button>

        <button
          class="tool-btn secondary icon"
          type="button"
          @click="toggleFavorite"
        >
          {{ isFavorite ? "★" : "☆" }}
        </button>

        <button
          class="tool-btn"
          type="button"
          :disabled="loading"
          @click="playCurrent"
        >
          播放
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
          class="tool-btn secondary icon"
          type="button"
          :disabled="currentParagraphIndex <= 0"
          @click="goPrev"
        >
          ◀
        </button>

        <button
          class="tool-btn icon"
          type="button"
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
import {
  getArticleByNoteNode,
  updateArticleFavorite,
  updateArticlePlaybackRate,
  updateArticlePosition,
} from "@/api/articles";
import {
  createDefaultArticle,
  normalizeArticle,
} from "@/model/article/article";
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
      article: createDefaultArticle(),
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
    maxParagraphIndex() {
      return Math.max(
        0,
        ((this.article.paragraphs && this.article.paragraphs.length) || 1) - 1
      );
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
        const data = await getArticleByNoteNode(this.noteNodeId);
        this.article = normalizeArticle(data);
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
    toggleRuby() {
      this.rubyVisible = !this.rubyVisible;
    },
    async toggleFavorite() {
      if (!this.article.id) {
        return;
      }
      try {
        const updated = await updateArticleFavorite(
          this.article.id,
          !this.isFavorite
        );
        this.article = normalizeArticle({ ...this.article, ...updated });
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
        const updated = await updateArticlePlaybackRate(
          this.article.id,
          nextRate
        );
        this.article = normalizeArticle({ ...this.article, ...updated });
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
        const updated = await updateArticlePosition(this.article.id, index);
        this.article = normalizeArticle({ ...this.article, ...updated });
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

.section {
  margin-top: 18px;
}

.paragraph-block {
  border: 1px solid transparent;
  border-radius: 14px;
  margin-bottom: 12px;
  transition: border-color 0.2s ease;
}

.paragraph-block.active {
  border-color: #c8ad8d;
}

.jp-paragraph {
  margin: 0;
  padding: 16px 14px;
  background: #fffaf3;
  border-radius: 14px;
  border: 1px solid #f0e5d6;
  font-size: 22px;
  line-height: 2.3;
  letter-spacing: 0.02em;
  color: #1f1a14;
  word-break: break-word;
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
  margin-top: 8px;
  padding: 12px 14px;
  background: #f8f3ec;
  border-radius: 12px;
  color: #6a5746;
  font-size: 14px;
  line-height: 1.9;
}

.toolbar {
  display: flex;
  gap: 10px;
  flex-wrap: nowrap;
  padding: 12px 16px;
  border-top: 1px solid #e8e0d5;
  background: rgba(251, 248, 243, 0.96);
  backdrop-filter: blur(8px);
  overflow-x: auto;
  overflow-y: hidden;
}

.tool-btn {
  flex: 0 0 auto;
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
  min-width: 44px;
  padding: 12px 10px;
  font-size: 16px;
  line-height: 1;
}

.tool-btn.secondary {
  background: #efe7db;
  color: #5a4632;
}

.speed-select {
  flex: 0 0 auto;
  border: 1px solid #e0d5c6;
  border-radius: 12px;
  background: #fffdf9;
  color: #5a4632;
  padding: 0 10px;
  height: 40px;
  font-size: 13px;
  min-width: 88px;
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
