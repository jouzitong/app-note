<template>
  <div class="page">
    <div class="header">
      <div class="header-top">
        <div>
          <div class="header-title">日语学习工作台</div>
          <div class="header-desc">
            围绕 N5/N4/N3
            词库，管理每日新词、复习和做题，避免只学不练、只记不复盘。
          </div>
        </div>
        <div class="header-actions">
          <button
            v-if="!loggedIn"
            class="action-btn light"
            type="button"
            @click="goLogin"
          >
            登录
          </button>
          <button v-else class="action-btn" type="button" @click="handleLogout">
            退出登录
          </button>
        </div>
      </div>
    </div>

    <div class="search-box">
      <input
        class="search-input"
        type="text"
        placeholder="搜索词库、计划或笔记模块..."
      />
    </div>

    <div class="section">
      <div class="section-title">学习计划模块</div>
      <div class="section-subtitle">
        按“新词 30 + 复习 80 + 做题 2
        套”执行每日学习闭环，你可以按导入词量动态调整配额。
      </div>

      <div class="panel">
        <div class="hero-grid">
          <div class="hero-main">
            <h3>今日总览</h3>
            <div class="kpi-row">
              <div v-for="kpi in kpis" :key="kpi.label" class="kpi">
                <strong>{{ kpi.value }}</strong>
                <span>{{ kpi.label }}</span>
              </div>
            </div>
            <div class="progress-wrap">
              <div class="progress-meta">
                <span>当日完成度</span>
                <span>{{ progressRate }}%</span>
              </div>
              <div class="progress">
                <i :style="{ width: `${progressRate}%` }"></i>
              </div>
            </div>
          </div>
          <div class="hero-side">
            <h4>今日建议节奏</h4>
            <div class="chips">
              <span
                v-for="item in schedules"
                :key="item.text"
                class="chip"
                :class="item.level"
              >
                {{ item.text }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <div class="panel">
        <div class="section-title section-inline-title">N5/N4/N3 每日配额</div>
        <div class="plan-grid">
          <div v-for="plan in plans" :key="plan.level" class="plan-card">
            <div class="plan-head">
              <div class="plan-level">{{ plan.level }}</div>
              <span class="chip" :class="plan.levelClass">{{ plan.tag }}</span>
            </div>
            <div class="plan-target">{{ plan.target }}</div>
            <div class="plan-desc">{{ plan.desc }}</div>
          </div>
        </div>
      </div>

      <div class="panel">
        <div class="section-title section-inline-title">每日任务清单</div>
        <div class="task-list">
          <div v-for="task in tasks" :key="task.title" class="task-item">
            <div class="task-left">
              <span class="task-dot" :class="task.level"></span>
              <div>
                <div class="task-title">{{ task.title }}</div>
                <div class="task-desc">{{ task.desc }}</div>
              </div>
            </div>
            <span class="task-status">{{ task.status }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="section">
      <div class="section-title">笔记模块</div>
      <div class="section-subtitle">
        把学习行为沉淀成可复用资料：错词、题目、语法和表达。
      </div>
      <div class="card-list">
        <button
          v-for="card in cards"
          :key="card.title"
          type="button"
          class="card-item"
          @click="card.action"
        >
          <div class="card-icon">{{ card.icon }}</div>
          <div class="card-title">{{ card.title }}</div>
          <div class="card-desc">{{ card.desc }}</div>
        </button>
      </div>
    </div>

    <div class="footer">建议每周日回顾一次计划配额，按完成率调整次周新词量</div>
  </div>
</template>

<script>
import { logout } from "@/api/auth";
import { clearAuth, hasAuthToken } from "@/utils/auth";

export default {
  name: "HomeView",
  data() {
    return {
      progressRate: 42,
      kpis: [
        { value: "30", label: "新词目标" },
        { value: "80", label: "复习目标" },
        { value: "2 套", label: "做题目标" },
      ],
      schedules: [
        { text: "08:00 新词 15", level: "ok" },
        { text: "12:30 复习 40", level: "" },
        { text: "19:30 新词 15", level: "warn" },
        { text: "21:00 复习 40", level: "" },
        { text: "21:30 做题 2 套", level: "danger" },
      ],
      plans: [
        {
          level: "N5",
          tag: "基础巩固",
          levelClass: "ok",
          target: "10 词/天",
          desc: "已导入 780 词，建议优先完成，压低遗忘率。",
        },
        {
          level: "N4",
          tag: "核心推进",
          levelClass: "warn",
          target: "12 词/天",
          desc: "已导入 1120 词，搭配语法点和短句做题。",
        },
        {
          level: "N3",
          tag: "高频突破",
          levelClass: "danger",
          target: "8 词/天",
          desc: "已导入 1350 词，建议按主题集中记忆。",
        },
      ],
      tasks: [
        {
          level: "ok",
          title: "新词学习（30/30）",
          desc: "N5:10 + N4:12 + N3:8，建议分 2 次完成。",
          status: "已完成",
        },
        {
          level: "warn",
          title: "当日复习（52/80）",
          desc: "按 1-3-7-14 天复习节奏，优先处理昨日新词。",
          status: "进行中",
        },
        {
          level: "danger",
          title: "做题训练（0/2 套）",
          desc: "建议 1 套词汇题 + 1 套阅读题，提交后记录错词。",
          status: "待开始",
        },
      ],
    };
  },
  computed: {
    loggedIn() {
      return hasAuthToken();
    },
    cards() {
      return [
        {
          icon: "🈶",
          title: "词汇笔记",
          desc: "记录新词释义、例句、易混词与记忆法。",
          action: this.openNoteModule,
        },
        {
          icon: "♻️",
          title: "复习错词本",
          desc: "自动收纳复习错误词，按错误次数分层复盘。",
          action: this.openWordCardModule,
        },
        {
          icon: "🧩",
          title: "刷题记录",
          desc: "按题型统计正确率，定位薄弱知识点。",
          action: this.openArticleModule,
        },
        {
          icon: "📌",
          title: "语法与表达",
          desc: "整理 N4/N3 重点语法和高频表达模板。",
          action: this.openNoteModule,
        },
      ];
    },
  },
  methods: {
    goLogin() {
      this.$router.push({ name: "login" });
    },
    async handleLogout() {
      try {
        await logout();
      } catch (error) {
        console.warn("logout failed", error);
      } finally {
        clearAuth();
        this.$router.push({ name: "home" });
      }
    },
    requireLogin(next) {
      if (!this.loggedIn) {
        const targetPath = this.$router.resolve(next).route.fullPath;
        this.$router.push({ name: "login", query: { redirect: targetPath } });
        return;
      }
      this.$router.push(next);
    },
    openNoteModule() {
      this.requireLogin({ name: "note", params: { id: "1" } });
    },
    openWordCardModule() {
      this.requireLogin({
        name: "word-card",
        params: { parentId: "1" },
        query: { nodeId: "1", pageIndex: "1", wordIndex: "0" },
      });
    },
    openArticleModule() {
      this.requireLogin({
        name: "article-reader",
        params: { parentId: "1" },
        query: { nodeId: "1" },
      });
    },
  },
};
</script>

<style scoped>
:root {
  --bg: #f3f6fb;
  --panel: #ffffff;
  --text: #1f2937;
  --muted: #6b7280;
  --line: #e5e7eb;
  --brand: #2563eb;
  --ok: #059669;
  --warn: #d97706;
  --danger: #dc2626;
}

* {
  box-sizing: border-box;
}

.page {
  max-width: 980px;
  margin: 0 auto;
  min-height: 100vh;
  padding: 18px 16px 28px;
  color: var(--text);
  background: radial-gradient(circle at 0% 0%, #e0ecff 0, transparent 36%),
    radial-gradient(circle at 100% 100%, #e7f8ee 0, transparent 38%), var(--bg);
}

.header {
  background: linear-gradient(135deg, #1d4ed8, #0ea5e9);
  color: #fff;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
  margin-bottom: 16px;
}

.header-top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.header-title {
  font-size: 26px;
  font-weight: 700;
  margin-bottom: 8px;
}

.header-desc {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.6;
}

.header-actions {
  align-self: flex-start;
}

.action-btn {
  border: 0;
  background: #0f766e;
  color: #fff;
  height: 36px;
  padding: 0 14px;
  border-radius: 10px;
  cursor: pointer;
}

.action-btn.light {
  background: #ffffff;
  color: #1f2937;
}

.search-box {
  margin: 14px 0 20px;
}

.search-input {
  width: 100%;
  height: 46px;
  border: 1px solid #e2e8f0;
  outline: none;
  border-radius: 14px;
  padding: 0 14px;
  font-size: 14px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(2, 6, 23, 0.04);
}

.section {
  margin-bottom: 18px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 12px;
}

.section-inline-title {
  font-size: 16px;
  margin-bottom: 10px;
}

.section-subtitle {
  font-size: 13px;
  color: var(--muted);
  margin-bottom: 12px;
  line-height: 1.6;
}

.panel {
  background: var(--panel);
  border-radius: 18px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
  padding: 16px;
  margin-bottom: 12px;
}

.hero-grid {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 12px;
}

.hero-main {
  background: linear-gradient(145deg, #eff6ff, #f0f9ff);
  border: 1px solid #dbeafe;
  border-radius: 14px;
  padding: 14px;
}

.hero-main h3 {
  font-size: 15px;
  margin-bottom: 8px;
}

.kpi-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin: 10px 0 12px;
}

.kpi {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 8px;
  text-align: center;
}

.kpi strong {
  display: block;
  font-size: 18px;
  line-height: 1.2;
}

.kpi span {
  font-size: 12px;
  color: var(--muted);
}

.progress-wrap {
  margin-top: 8px;
}

.progress-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--muted);
  margin-bottom: 6px;
}

.progress {
  width: 100%;
  height: 8px;
  background: #e5e7eb;
  border-radius: 999px;
  overflow: hidden;
}

.progress > i {
  display: block;
  height: 100%;
  background: linear-gradient(90deg, #2563eb, #06b6d4);
  border-radius: 999px;
}

.hero-side {
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 14px;
}

.hero-side h4 {
  font-size: 14px;
  margin-bottom: 10px;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  font-size: 12px;
  padding: 6px 10px;
  border-radius: 999px;
  background: #f3f4f6;
  color: #374151;
}

.chip.ok {
  background: #ecfdf5;
  color: var(--ok);
}

.chip.warn {
  background: #fffbeb;
  color: var(--warn);
}

.chip.danger {
  background: #fef2f2;
  color: var(--danger);
}

.plan-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.plan-card {
  border: 1px solid var(--line);
  border-radius: 14px;
  padding: 12px;
  background: #fff;
}

.plan-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.plan-level {
  font-size: 14px;
  font-weight: 700;
}

.plan-target {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 2px;
}

.plan-desc {
  font-size: 12px;
  color: var(--muted);
}

.task-list {
  display: grid;
  gap: 10px;
}

.task-item {
  border: 1px solid var(--line);
  border-radius: 14px;
  padding: 12px;
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.task-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.task-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: #9ca3af;
}

.task-dot.ok {
  background: var(--ok);
}

.task-dot.warn {
  background: var(--warn);
}

.task-dot.danger {
  background: var(--danger);
}

.task-title {
  font-size: 14px;
  font-weight: 600;
}

.task-desc {
  font-size: 12px;
  color: var(--muted);
  margin-top: 2px;
}

.task-status {
  font-size: 12px;
  color: #374151;
  padding: 6px 10px;
  background: #f3f4f6;
  border-radius: 999px;
  white-space: nowrap;
}

.card-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.card-item {
  text-align: left;
  background: #fff;
  border-radius: 16px;
  padding: 14px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid #eef2f7;
  color: inherit;
  transition: transform 0.2s ease;
  cursor: pointer;
}

.card-item:active {
  transform: scale(0.98);
}

.card-icon {
  font-size: 23px;
  margin-bottom: 8px;
}

.card-title {
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 4px;
}

.card-desc {
  font-size: 12px;
  line-height: 1.55;
  color: var(--muted);
}

.footer {
  margin-top: 18px;
  text-align: center;
  font-size: 12px;
  color: #9ca3af;
}

@media (max-width: 768px) {
  .header-top {
    flex-direction: column;
  }

  .hero-grid {
    grid-template-columns: 1fr;
  }

  .plan-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 420px) {
  .card-list {
    grid-template-columns: 1fr;
  }

  .kpi-row {
    grid-template-columns: 1fr;
  }
}
</style>
