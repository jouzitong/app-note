<template>
  <ArticleReader :article-id="resolvedArticleId" />
</template>

<script>
import ArticleReader from "@/components/article/ArticleReader.vue";

export default {
  name: "ArticleReaderIndexView",
  components: {
    ArticleReader,
  },
  computed: {
    resolvedArticleId() {
      const params = (this.$route && this.$route.params) || {};
      const articleId = params.id;
      if (articleId === undefined || articleId === null) {
        return "article_001";
      }
      const normalized = String(articleId).trim();
      return normalized || "article_001";
    },
  },
};
</script>
