<template>
  <div class="article-page">
    <ArticleReader
      v-if="resolvedNodeId !== null"
      :note-node-id="resolvedNodeId"
      @back="goToParentNote"
    />
    <div v-else class="note-id-missing">缺少 nodeId 参数</div>
  </div>
</template>

<script>
import ArticleReader from "@/components/article/ArticleReader.vue";

export default {
  name: "ArticleReaderIndexView",
  components: {
    ArticleReader,
  },
  computed: {
    resolvedParentId() {
      return this.parsePositiveInt(this.$route.params.parentId);
    },
    resolvedNodeId() {
      return this.parsePositiveInt(this.$route.query.nodeId);
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
    goToParentNote() {
      if (this.resolvedParentId === null) {
        this.$router.back();
        return;
      }
      this.$router.push({
        name: "note",
        params: { id: String(this.resolvedParentId) },
      });
    },
  },
};
</script>

<style scoped>
.article-page {
  min-height: 100vh;
}

.note-id-missing {
  margin-top: 40px;
  color: #ef4444;
  font-size: 14px;
  text-align: center;
}
</style>
