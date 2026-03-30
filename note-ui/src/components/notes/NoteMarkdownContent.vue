<template>
  <div class="card">
    <h2 class="section-title">笔记内容</h2>
    <div class="note-content">
      <p>{{ contentData.paragraphs[0] }}</p>

      <h3>内容示例</h3>
      <p>{{ contentData.paragraphs[1] }}</p>

      <ul>
        <li v-for="item in contentData.bullets" :key="item">{{ item }}</li>
      </ul>

      <h3>适合的展示方式</h3>
      <p>{{ contentData.paragraphs[2] }}</p>
    </div>
  </div>
</template>

<script>
export default {
  name: "NoteMarkdownContent",
  props: {
    contentData: {
      type: Object,
      required: true,
    },
  },
};
</script>

<style scoped>
.card {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 6px 20px rgba(15, 23, 42, 0.06);
  padding: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 14px;
  color: #111827;
}

.note-content {
  line-height: 1.9;
  font-size: 15px;
  color: #374151;
}

.note-content h3 {
  margin-top: 24px;
  margin-bottom: 10px;
  font-size: 17px;
  color: #111827;
}

.note-content p {
  margin: 0 0 14px;
}

.note-content ul {
  margin: 0 0 14px 20px;
  padding: 0;
}
</style>
