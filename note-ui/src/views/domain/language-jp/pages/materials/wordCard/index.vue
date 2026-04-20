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
  watch: {
    "$route.fullPath"() {
      this.syncFromRoute();
    },
  },
  methods: {
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
}

.note-id-missing {
  margin-top: 40px;
  color: #ef4444;
  font-size: 14px;
}

@media (min-width: 640px) {
  .word-card-page {
    padding: 20px;
  }
}
</style>
