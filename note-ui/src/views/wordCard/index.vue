<template>
  <div class="word-card-page">
    <WordCard
      v-if="resolvedNoteId !== null"
      :note-id="resolvedNoteId"
      :index="0"
    />
    <div v-else class="note-id-missing">缺少 noteId 参数</div>
  </div>
</template>

<script>
import WordCard from "@/components/word/WordCard.vue";

export default {
  name: "WordCardIndexView",
  components: {
    WordCard,
  },
  computed: {
    resolvedNoteId() {
      const routeNoteId = this.$route.params.noteId || this.$route.query.noteId;
      if (
        routeNoteId === undefined ||
        routeNoteId === null ||
        routeNoteId === ""
      ) {
        return null;
      }
      return routeNoteId;
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
