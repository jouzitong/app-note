<template>
  <main class="error-page">
    <section class="error-card">
      <h1>系统异常</h1>
      <p>服务暂时不可用，请稍后再试。</p>
      <div class="actions">
        <button type="button" class="secondary" @click="goBack">
          返回上页
        </button>
        <button type="button" @click="goHome">返回首页</button>
      </div>
    </section>
  </main>
</template>

<script>
export default {
  name: "ErrorPage",
  methods: {
    goBack() {
      this.$router.back();
    },
    goHome() {
      this.$router.replace({ name: "note-home" });
    },
  },
};
</script>

<style scoped>
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-5);
  background: var(--color-surface-page);
}

.error-card {
  width: 100%;
  max-width: 440px;
  border-radius: 14px;
  background: var(--color-surface-card);
  border: 1px solid var(--color-border-default);
  box-shadow: 0 10px 24px rgba(124, 45, 18, 0.12);
  text-align: center;
  padding: var(--space-6);
}

.error-card h1 {
  margin: 0 0 8px;
  font-size: 26px;
  color: var(--color-error-text);
}

.error-card p {
  margin: 0 0 16px;
  color: var(--color-text-muted);
}

.actions {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.actions button {
  border: 0;
  border-radius: 8px;
  padding: 10px 14px;
  color: #fff;
  background: var(--color-brand-primary);
  cursor: pointer;
}

.actions button.secondary {
  background: var(--color-text-muted);
}
</style>
