<template>
  <main class="not-found-page">
    <section class="not-found-card">
      <h1>404</h1>
      <p>页面不存在或已被移动。</p>
      <button type="button" @click="goHome">返回首页</button>
    </section>
  </main>
</template>

<script>
export default {
  name: "NotFoundPage",
  methods: {
    goHome() {
      this.$router.replace({ name: "note-home" });
    },
  },
};
</script>

<style scoped>
.not-found-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-5);
  background: var(--color-surface-page);
}

.not-found-card {
  width: 100%;
  max-width: 420px;
  border-radius: 14px;
  background: var(--color-surface-card);
  border: 1px solid var(--color-border-default);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08);
  text-align: center;
  padding: var(--space-6);
}

.not-found-card h1 {
  margin: 0 0 8px;
  font-size: 32px;
}

.not-found-card p {
  margin: 0 0 16px;
  color: var(--color-text-muted);
}

.not-found-card button {
  border: 0;
  border-radius: 8px;
  padding: 10px var(--space-4);
  background: var(--color-brand-primary);
  color: #fff;
  cursor: pointer;
}
</style>
