<template>
  <div class="word-card-page-demo">
    <WordCard :note-id="8" :index="0" />
  </div>
</template>

<script>
import WordCard from "@/components/word/WordCard.vue";

export default {
  name: "WordCardTestView",
  components: {
    WordCard,
  },
};
</script>

<style scoped>
.word-card-page-demo {
  min-height: 100%;
  margin: 0;
  padding: 8px;
  background: #f4f6fb;
  text-align: left;
}

@media (min-width: 640px) {
  .word-card-page-demo {
    padding: 20px;
  }
}
</style>
