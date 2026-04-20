<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="login-title">app-note 登录</h1>
      <p class="login-subtitle">使用 athena 安全模块认证（/auth/login）</p>

      <form class="login-form" @submit.prevent="handleSubmit">
        <label class="field">
          <span>用户名</span>
          <input
            v-model.trim="form.username"
            type="text"
            placeholder="请输入用户名"
            autocomplete="username"
            :disabled="loading"
          />
        </label>

        <label class="field">
          <span>密码</span>
          <input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
            :disabled="loading"
          />
        </label>

        <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

        <button class="submit-btn" type="submit" :disabled="loading">
          {{ loading ? "登录中..." : "登录" }}
        </button>
      </form>

      <button class="back-home" type="button" @click="goHome">返回首页</button>
    </div>
  </div>
</template>

<script>
import { login } from "@/api/auth";
import { saveAuthToken, saveAuthUser } from "@/utils/auth";
import { getErrorMessage } from "@/utils/error";

export default {
  name: "LoginView",
  data() {
    return {
      loading: false,
      errorMessage: "",
      form: {
        username: "",
        password: "",
      },
    };
  },
  methods: {
    goHome() {
      this.$router.push({ name: "note-home" });
    },
    resolveRedirect() {
      const redirect = this.$route.query.redirect;
      if (typeof redirect === "string" && redirect.trim()) {
        return redirect;
      }
      return "/";
    },
    async handleSubmit() {
      if (!this.form.username || !this.form.password) {
        this.errorMessage = "用户名和密码不能为空";
        return;
      }

      this.loading = true;
      this.errorMessage = "";
      try {
        const result = await login(this.form.username, this.form.password);
        const token = result?.token;
        if (!token) {
          throw new Error("登录成功但未返回 token");
        }
        saveAuthToken(token);
        saveAuthUser(result?.user || null);
        this.$router.replace(this.resolveRedirect());
      } catch (error) {
        this.errorMessage = getErrorMessage(error) || "登录失败，请稍后重试";
      } finally {
        this.loading = false;
      }
    },
  },
};
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-5);
  background: radial-gradient(circle at 10% 10%, #dbeafe 0, transparent 30%),
    radial-gradient(circle at 90% 90%, #dcfce7 0, transparent 34%),
    var(--color-surface-page);
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: var(--color-surface-card);
  border-radius: 16px;
  box-shadow: 0 20px 45px rgba(15, 23, 42, 0.12);
  border: 1px solid var(--color-border-default);
  padding: var(--space-6);
}

.login-title {
  margin: 0;
  font-size: 24px;
  font-weight: var(--font-weight-bold);
  color: var(--color-text-strong);
}

.login-subtitle {
  margin: var(--space-2) 0 var(--space-5);
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.login-form {
  display: grid;
  gap: var(--space-3);
}

.field {
  display: grid;
  gap: 6px;
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
}

.field input {
  height: 40px;
  border: 1px solid var(--color-border-default);
  border-radius: 10px;
  padding: 0 var(--space-3);
  outline: none;
  font-size: var(--font-size-md);
}

.field input:focus {
  border-color: var(--color-brand-primary);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
}

.error-message {
  margin: 0;
  color: var(--color-error-text);
  font-size: var(--font-size-xs);
}

.submit-btn {
  height: 42px;
  border: 0;
  border-radius: 10px;
  background: linear-gradient(
    135deg,
    var(--color-brand-primary),
    var(--color-brand-accent)
  );
  color: #fff;
  font-weight: var(--font-weight-semibold);
  cursor: pointer;
}

.submit-btn:disabled {
  opacity: 0.75;
  cursor: not-allowed;
}

.tips {
  margin-top: var(--space-4);
  padding: var(--space-3);
  border-radius: 10px;
  background: var(--color-surface-page);
}

.tip-title {
  font-size: var(--font-size-xs);
  color: var(--color-text-strong);
  font-weight: var(--font-weight-semibold);
  margin-bottom: 6px;
}

.tip-text {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
  line-height: 1.5;
}

.back-home {
  margin-top: 14px;
  border: 0;
  background: transparent;
  color: var(--color-brand-primary);
  cursor: pointer;
  padding: 0;
}
</style>
