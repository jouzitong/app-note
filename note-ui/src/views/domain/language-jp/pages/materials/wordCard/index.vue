<template>
  <div class="word-card-page">
    <WordCard
      v-if="resolvedNodeId !== null"
      :note-id="resolvedNodeId"
      :index="currentIndex"
      @update:index="handleIndexUpdate"
      @back="goToParentNote"
    />
    <div v-else class="note-id-missing">缺少 nodeId 参数</div>
  </div>
</template>

<script>
import WordCard from "@/components/word/WordCard.vue";

export default {
  name: "WordCardIndexView",
  components: {
    WordCard,
  },
  data() {
    return {
      currentIndex: 0,
      pageSize: 10,
      onGestureStart: null,
      onGestureChange: null,
      onGestureEnd: null,
      onTouchMove: null,
      onWheel: null,
    };
  },
  computed: {
    resolvedParentId() {
      return this.parsePositiveInt(this.$route.params.parentId);
    },
    resolvedNodeId() {
      return this.parsePositiveInt(this.$route.query.nodeId);
    },
    resolvedPageIndex() {
      const pageIndex = this.parsePositiveInt(this.$route.query.pageIndex);
      return pageIndex || 1;
    },
    resolvedWordIndex() {
      const wordIndex = Number(this.$route.query.wordIndex);
      if (Number.isInteger(wordIndex) && wordIndex >= 0) {
        return wordIndex;
      }
      return (this.resolvedPageIndex - 1) * this.pageSize;
    },
  },
  created() {
    this.syncFromRoute();
  },
  mounted() {
    this.installInteractionGuards();
  },
  beforeDestroy() {
    this.uninstallInteractionGuards();
  },
  watch: {
    "$route.fullPath"() {
      this.syncFromRoute();
    },
  },
  methods: {
    installInteractionGuards() {
      this.onGestureStart = (event) => event.preventDefault();
      this.onGestureChange = (event) => event.preventDefault();
      this.onGestureEnd = (event) => event.preventDefault();
      this.onTouchMove = (event) => {
        if (event.touches && event.touches.length > 1) {
          event.preventDefault();
        }
      };
      this.onWheel = (event) => {
        if (event.ctrlKey) {
          event.preventDefault();
        }
      };

      document.addEventListener("gesturestart", this.onGestureStart, {
        passive: false,
      });
      document.addEventListener("gesturechange", this.onGestureChange, {
        passive: false,
      });
      document.addEventListener("gestureend", this.onGestureEnd, {
        passive: false,
      });
      document.addEventListener("touchmove", this.onTouchMove, {
        passive: false,
      });
      document.addEventListener("wheel", this.onWheel, { passive: false });
    },
    uninstallInteractionGuards() {
      if (this.onGestureStart) {
        document.removeEventListener("gesturestart", this.onGestureStart);
      }
      if (this.onGestureChange) {
        document.removeEventListener("gesturechange", this.onGestureChange);
      }
      if (this.onGestureEnd) {
        document.removeEventListener("gestureend", this.onGestureEnd);
      }
      if (this.onTouchMove) {
        document.removeEventListener("touchmove", this.onTouchMove);
      }
      if (this.onWheel) {
        document.removeEventListener("wheel", this.onWheel);
      }
    },
    parsePositiveInt(value) {
      if (value === undefined || value === null || value === "") {
        return null;
      }
      const parsed = Number(value);
      if (!Number.isInteger(parsed) || parsed <= 0) {
        return null;
      }
      return parsed;
    },
    syncFromRoute() {
      this.currentIndex = this.resolvedWordIndex;
    },
    handleIndexUpdate(nextIndex) {
      this.currentIndex = nextIndex;
      if (this.resolvedParentId === null || this.resolvedNodeId === null) {
        return;
      }
      const pageIndex = Math.floor(nextIndex / this.pageSize) + 1;
      this.$router.replace({
        name: "language-jp-word-card",
        params: { parentId: String(this.resolvedParentId) },
        query: {
          nodeId: String(this.resolvedNodeId),
          pageIndex: String(pageIndex),
          wordIndex: String(nextIndex),
        },
      });
    },
    goToParentNote() {
      const hasHistory = window.history.length > 1;
      if (hasHistory) {
        this.$router.back();
        return;
      }

      if (this.resolvedParentId === null) {
        this.$router.push({ name: "language-jp-home" });
        return;
      }

      this.$router.push({
        name: "language-jp-materials",
        params: { id: String(this.resolvedParentId) },
      });
    },
  },
};
</script>

<style scoped>
.word-card-page {
  min-height: 100vh;
  margin: 0;
  padding: 8px;
  background: #f4f6fb;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  user-select: none;
  -webkit-user-select: none;
  -webkit-touch-callout: none;
  -webkit-tap-highlight-color: transparent;
  touch-action: manipulation;
  overscroll-behavior: none;
}

.note-id-missing {
  margin-top: 40px;
  color: #ef4444;
  font-size: 14px;
}

.word-card-page :deep(input),
.word-card-page :deep(textarea),
.word-card-page :deep([contenteditable="true"]) {
  user-select: text;
  -webkit-user-select: text;
  -webkit-touch-callout: default;
}

@media (min-width: 640px) {
  .word-card-page {
    padding: 20px;
  }
}
</style>
