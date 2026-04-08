<template>
  <div class="mobile-page">
    <main class="content-scroll">
      <section class="card">
        <div class="card-title-row">
          <h2 class="card-title">今日总览</h2>
          <span class="pill">完成度 {{ progressRate }}%</span>
        </div>
        <div class="kpi-grid">
          <div v-for="item in overviewKpis" :key="item.label" class="kpi-item">
            <div class="kpi-value">{{ item.value }}</div>
            <div class="kpi-label">{{ item.label }}</div>
          </div>
        </div>
        <div class="progress-track">
          <i :style="{ width: `${progressRate}%` }"></i>
        </div>
      </section>

      <section class="card">
        <div class="card-title-row">
          <h2 class="card-title">今日打卡情况</h2>
          <span class="pill ghost"
            >{{ checkinDone }}/{{ checkinItems.length }}</span
          >
        </div>
        <div class="checkin-list">
          <div
            v-for="item in checkinItems"
            :key="item.title"
            class="checkin-item"
          >
            <span class="checkin-dot" :class="item.status"></span>
            <div class="checkin-main">
              <div class="checkin-title">{{ item.title }}</div>
              <div class="checkin-desc">{{ item.desc }}</div>
            </div>
            <span class="checkin-badge">{{ item.badge }}</span>
          </div>
        </div>
      </section>

      <section class="card">
        <h2 class="card-title">学习计划</h2>
        <div class="plan-list">
          <article v-for="plan in plans" :key="plan.level" class="plan-item">
            <div class="plan-top">
              <strong>{{ plan.level }}</strong>
              <span class="tag" :class="plan.levelClass">{{ plan.tag }}</span>
            </div>
            <div class="plan-target">{{ plan.target }}</div>
            <p class="plan-desc">{{ plan.desc }}</p>
          </article>
        </div>
      </section>

      <section class="card card-last">
        <div class="card-title-row">
          <h2 class="card-title">笔记模块</h2>
          <span class="pill ghost">4 个入口</span>
        </div>
        <div class="note-grid">
          <button
            v-for="card in noteCards"
            :key="card.title"
            type="button"
            class="note-item"
            @click="card.action"
          >
            <div class="note-icon">{{ card.icon }}</div>
            <div class="note-title">{{ card.title }}</div>
            <div class="note-desc">{{ card.desc }}</div>
          </button>
        </div>
      </section>
    </main>

    <nav class="bottom-nav" aria-label="主导航">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        type="button"
        class="nav-item"
        :class="{ active: activeTab === tab.key }"
        disabled
      >
        <span class="nav-icon">{{ tab.icon }}</span>
        <span class="nav-label">{{ tab.label }}</span>
      </button>
    </nav>
  </div>
</template>

<script>
import { hasAuthToken } from "@/utils/auth";

export default {
  name: "LanguageJpView",
  data() {
    return {
      progressRate: 42,
      activeTab: "home",
      overviewKpis: [
        { label: "新词", value: "30" },
        { label: "复习", value: "80" },
        { label: "做题", value: "2 套" },
        { label: "学习时长", value: "72 分钟" },
      ],
      checkinItems: [
        {
          title: "新词学习",
          desc: "N5:10 + N4:12 + N3:8",
          badge: "30/30",
          status: "ok",
        },
        {
          title: "复习巩固",
          desc: "按 1-3-7-14 天节奏推进",
          badge: "52/80",
          status: "warn",
        },
        {
          title: "做题训练",
          desc: "词汇 1 套 + 阅读 1 套",
          badge: "0/2",
          status: "danger",
        },
      ],
      plans: [
        {
          level: "N5",
          tag: "基础巩固",
          levelClass: "ok",
          target: "10 词/天",
          desc: "已导入 780 词，优先完成降低遗忘。",
        },
        {
          level: "N4",
          tag: "核心推进",
          levelClass: "warn",
          target: "12 词/天",
          desc: "已导入 1120 词，搭配语法做题。",
        },
        {
          level: "N3",
          tag: "高频突破",
          levelClass: "danger",
          target: "8 词/天",
          desc: "已导入 1350 词，建议按主题记忆。",
        },
      ],
      tabs: [
        { key: "home", label: "首页", icon: "⌂" },
        { key: "exam", label: "考试", icon: "✎" },
        { key: "course", label: "课程", icon: "▦" },
        { key: "plan", label: "计划", icon: "◷" },
        { key: "mine", label: "我的", icon: "◉" },
      ],
    };
  },
  computed: {
    loggedIn() {
      return hasAuthToken();
    },
    checkinDone() {
      return this.checkinItems.filter((item) => item.status === "ok").length;
    },
    noteCards() {
      return [
        {
          icon: "🈶",
          title: "词汇笔记",
          desc: "记录释义、例句、易混词",
          action: this.openNoteModule,
        },
        {
          icon: "♻",
          title: "错词复盘",
          desc: "按错误次数分层回顾",
          action: this.openWordCardModule,
        },
        {
          icon: "🧩",
          title: "刷题记录",
          desc: "定位薄弱题型",
          action: this.openArticleModule,
        },
        {
          icon: "✦",
          title: "语法表达",
          desc: "沉淀常用模板",
          action: this.openNoteModule,
        },
      ];
    },
  },
  methods: {
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
.mobile-page {
  height: 100vh;
  height: 100dvh;
  min-height: -webkit-fill-available;
  width: 100%;
  overflow: hidden;
  background: #f3f6fb;
  touch-action: pan-y;
}

.content-scroll {
  height: calc(100vh - 56px - env(safe-area-inset-bottom));
  height: calc(100dvh - 56px - env(safe-area-inset-bottom));
  overflow-y: auto;
  overflow-x: hidden;
  -webkit-overflow-scrolling: touch;
  overscroll-behavior-y: contain;
  padding: calc(14px + env(safe-area-inset-top)) 12px
    calc(16px + env(safe-area-inset-bottom));
}

.card {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
  padding: 14px;
  margin-bottom: 12px;
}

.card-last {
  margin-bottom: 2px;
}

.card-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.card-title {
  margin: 0;
  font-size: 16px;
  color: #111827;
}

.pill {
  background: #dbeafe;
  color: #1d4ed8;
  font-size: 11px;
  border-radius: 999px;
  padding: 4px 8px;
  white-space: nowrap;
}

.pill.ghost {
  background: #f3f4f6;
  color: #4b5563;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  margin-bottom: 10px;
}

.kpi-item {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 10px;
}

.kpi-value {
  font-size: 16px;
  font-weight: 700;
  color: #111827;
}

.kpi-label {
  margin-top: 2px;
  font-size: 12px;
  color: #6b7280;
}

.progress-track {
  width: 100%;
  height: 8px;
  border-radius: 999px;
  overflow: hidden;
  background: #e5e7eb;
}

.progress-track i {
  display: block;
  height: 100%;
  background: linear-gradient(90deg, #2563eb, #06b6d4);
}

.checkin-list {
  display: grid;
  gap: 8px;
}

.checkin-item {
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 10px;
}

.checkin-dot {
  width: 9px;
  height: 9px;
  border-radius: 999px;
  background: #9ca3af;
  flex: 0 0 auto;
}

.checkin-dot.ok {
  background: #059669;
}

.checkin-dot.warn {
  background: #d97706;
}

.checkin-dot.danger {
  background: #dc2626;
}

.checkin-main {
  min-width: 0;
  flex: 1;
}

.checkin-title {
  font-size: 13px;
  font-weight: 600;
  color: #111827;
}

.checkin-desc {
  margin-top: 2px;
  font-size: 11px;
  color: #6b7280;
}

.checkin-badge {
  font-size: 11px;
  background: #f3f4f6;
  border-radius: 999px;
  padding: 4px 8px;
  white-space: nowrap;
}

.plan-list {
  display: grid;
  gap: 8px;
}

.plan-item {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 10px;
  background: #fcfcfd;
}

.plan-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tag {
  border-radius: 999px;
  font-size: 11px;
  padding: 4px 8px;
  background: #f3f4f6;
  color: #4b5563;
}

.tag.ok {
  background: #ecfdf5;
  color: #047857;
}

.tag.warn {
  background: #fffbeb;
  color: #b45309;
}

.tag.danger {
  background: #fef2f2;
  color: #b91c1c;
}

.plan-target {
  margin-top: 8px;
  font-weight: 700;
  color: #111827;
}

.plan-desc {
  margin: 4px 0 0;
  font-size: 12px;
  color: #6b7280;
}

.note-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.note-item {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #fff;
  padding: 10px;
  text-align: left;
}

.note-icon {
  font-size: 18px;
}

.note-title {
  margin-top: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #111827;
}

.note-desc {
  margin-top: 2px;
  font-size: 11px;
  color: #6b7280;
  line-height: 1.4;
}

.bottom-nav {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  height: calc(56px + env(safe-area-inset-bottom));
  padding-bottom: calc(4px + env(safe-area-inset-bottom));
  background: #ffffff;
  border-top: 1px solid #e5e7eb;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  align-items: start;
  z-index: 10;
  transform: translateZ(0);
  -webkit-transform: translateZ(0);
}

.nav-item {
  border: 0;
  background: transparent;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: center;
  gap: 3px;
  padding-top: 4px;
  margin: 0;
  color: #6b7280;
  pointer-events: none;
  cursor: default;
}

.nav-item.active {
  color: #1d4ed8;
}

.nav-icon {
  font-size: 15px;
  line-height: 1;
}

.nav-label {
  font-size: 10px;
  line-height: 1;
}

@media (min-width: 768px) {
  .mobile-page {
    max-width: 430px;
    margin: 0 auto;
    border-left: 1px solid #e5e7eb;
    border-right: 1px solid #e5e7eb;
  }

  .bottom-nav {
    max-width: 430px;
    margin: 0 auto;
    left: 50%;
    transform: translateX(-50%) translateZ(0);
    -webkit-transform: translateX(-50%) translateZ(0);
  }
}
</style>
