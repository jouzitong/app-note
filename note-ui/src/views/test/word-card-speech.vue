<template>
  <div class="word-card-speech-demo-page">
    <div class="demo-header">
      <h2>WordCard 例句语音转文字 Demo</h2>
      <p>
        每条例句下方都有一个语音转写输入框。点击麦克风后开始录音，识别结果会实时更新在输入框中。
      </p>
    </div>

    <WordCard
      :note-id="8"
      :index="currentIndex"
      @update:index="currentIndex = $event"
    />
  </div>
</template>

<script>
import WordCard from "@/components/word/WordCard.vue";

export default {
  name: "WordCardSpeechTestView",
  components: {
    WordCard,
  },
  data() {
    return {
      currentIndex: 0,
    };
  },
};
</script>

<style scoped>
.word-card-speech-demo-page {
  min-height: 100%;
  margin: 0;
  padding: 8px;
  background: #f4f6fb;
  text-align: left;
}

.demo-header {
  max-width: 420px;
  margin-bottom: 10px;
  color: #1f2937;
}

.demo-header h2 {
  margin: 0 0 6px;
  font-size: 16px;
}

.demo-header p {
  margin: 0;
  font-size: 13px;
  line-height: 1.4;
  color: #4b5563;
}

@media (min-width: 640px) {
  .word-card-speech-demo-page {
    padding: 20px;
  }
}
</style>
