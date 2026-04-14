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

      <div class="tips">
        <div class="tip-title">测试账号（按你后端初始化数据）</div>
        <div class="tip-text">admin / admin123</div>
        <div class="tip-text">operator / op123</div>
        <div class="tip-text">guest / guest123</div>
      </div>

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
  padding: 20px;
  background: radial-gradient(circle at 10% 10%, #dbeafe 0, transparent 30%),
    radial-gradient(circle at 90% 90%, #dcfce7 0, transparent 34%), #f8fafc;
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 45px rgba(15, 23, 42, 0.12);
  border: 1px solid #e5e7eb;
  padding: 24px;
}

.login-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #111827;
}

.login-subtitle {
  margin: 8px 0 20px;
  color: #6b7280;
  font-size: 13px;
}

.login-form {
  display: grid;
  gap: 12px;
}

.field {
  display: grid;
  gap: 6px;
  font-size: 13px;
  color: #374151;
}

.field input {
  height: 40px;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 0 12px;
  outline: none;
  font-size: 14px;
}

.field input:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
}

.error-message {
  margin: 0;
  color: #dc2626;
  font-size: 12px;
}

.submit-btn {
  height: 42px;
  border: 0;
  border-radius: 10px;
  background: linear-gradient(135deg, #2563eb, #0ea5e9);
  color: #fff;
  font-weight: 600;
  cursor: pointer;
}

.submit-btn:disabled {
  opacity: 0.75;
  cursor: not-allowed;
}

.tips {
  margin-top: 16px;
  padding: 12px;
  border-radius: 10px;
  background: #f3f4f6;
}

.tip-title {
  font-size: 12px;
  color: #111827;
  font-weight: 600;
  margin-bottom: 6px;
}

.tip-text {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.5;
}

.back-home {
  margin-top: 14px;
  border: 0;
  background: transparent;
  color: #2563eb;
  cursor: pointer;
  padding: 0;
}
</style>
