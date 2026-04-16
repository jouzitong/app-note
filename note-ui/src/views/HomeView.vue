<template>
  <div class="page">
    <div class="search">
      <input type="text" placeholder="搜索模块、标签或最近笔记..." />
    </div>

    <section class="panel">
      <h2 class="panel-title">笔记模块选择</h2>
      <p class="panel-sub">系统核心是笔记。先选择知识模块，再进入对应专题。</p>

      <div class="module-grid">
        <button type="button" class="module-card" @click="goLanguageJp">
          <div class="module-icon">あ</div>
          <h3 class="module-title">日语学习笔记</h3>
          <p class="module-desc">词汇、语法、例句、错题与复习记录。</p>
          <span class="module-meta">最近更新：今天</span>
        </button>

        <button type="button" class="module-card" disabled>
          <div class="module-icon">⌘</div>
          <h3 class="module-title">计算机知识笔记</h3>
          <p class="module-desc">算法、后端、数据库、工程实践与问题复盘。</p>
          <span class="module-meta">最近更新：昨天</span>
        </button>

        <button type="button" class="module-card" disabled>
          <div class="module-icon">⚗</div>
          <h3 class="module-title">实验与项目笔记</h3>
          <p class="module-desc">需求拆解、方案设计、实验过程与结论沉淀。</p>
          <span class="module-meta">最近更新：3 天前</span>
        </button>

        <button type="button" class="module-card" disabled>
          <div class="module-icon">☰</div>
          <h3 class="module-title">通用知识库</h3>
          <p class="module-desc">跨模块通用模板、术语表、常用片段和清单。</p>
          <span class="module-meta">最近更新：1 周前</span>
        </button>
      </div>
    </section>

    <div class="split">
      <section class="panel">
        <h2 class="panel-title">今日待办</h2>
        <p class="panel-sub">围绕“写笔记、补标签、做归档”推进知识沉淀。</p>
        <div class="todo-list">
          <div class="todo-item">
            <div class="left">
              <p class="title">补充日语 N3 语法笔记</p>
              <div class="desc">整理 5 个易混语法点并补例句</div>
            </div>
            <span class="badge">进行中</span>
          </div>
          <div class="todo-item">
            <div class="left">
              <p class="title">计算机模块新增“缓存”专题</p>
              <div class="desc">先搭目录，再填入 3 篇核心笔记</div>
            </div>
            <span class="badge">待开始</span>
          </div>
          <div class="todo-item">
            <div class="left">
              <p class="title">清理未归档临时笔记</p>
              <div class="desc">统一补标签：source、topic、level</div>
            </div>
            <span class="badge">待开始</span>
          </div>
        </div>
      </section>

      <section class="panel">
        <h2 class="panel-title">最近访问</h2>
        <p class="panel-sub">保留固定入口，减少层级跳转。</p>
        <div class="todo-list">
          <div class="todo-item">
            <div class="left">
              <p class="title">日语 / 词汇复盘</p>
              <div class="desc">错词 12 条待回顾</div>
            </div>
            <span class="badge">打开</span>
          </div>
          <div class="todo-item">
            <div class="left">
              <p class="title">计算机 / MySQL 索引</p>
              <div class="desc">已沉淀 18 条优化结论</div>
            </div>
            <span class="badge">打开</span>
          </div>
          <div class="todo-item">
            <div class="left">
              <p class="title">项目 / app-note 改造记录</p>
              <div class="desc">待补登录与跨域复盘</div>
            </div>
            <span class="badge">打开</span>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
export default {
  name: "NoteHomeView",
  methods: {
    goLanguageJp() {
      this.$router.push({ name: "language-jp-home" });
    },
  },
};
</script>

<style scoped>
.page {
  max-width: 920px;
  margin: 0 auto;
  min-height: 100vh;
  padding: 18px 14px 28px;
  background: radial-gradient(circle at 0% 0%, #dcfce7 0, transparent 32%),
    radial-gradient(circle at 100% 100%, #dbeafe 0, transparent 34%), #f4f7fb;
}

.search {
  margin-bottom: 12px;
}

.search input {
  width: 100%;
  height: 44px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 0 12px;
  font-size: 14px;
  outline: none;
  background: #fff;
  box-shadow: 0 2px 8px rgba(2, 6, 23, 0.04);
}

.panel {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.08);
  padding: 14px;
  margin-bottom: 12px;
}

.panel-title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
}

.panel-sub {
  margin: 6px 0 12px;
  font-size: 12px;
  color: #64748b;
  line-height: 1.6;
}

.module-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.module-card {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #fff;
  padding: 12px;
  text-decoration: none;
  color: inherit;
  text-align: left;
  transition: transform 0.2s ease;
}

.module-card:active {
  transform: scale(0.98);
}

.module-card:disabled {
  opacity: 0.68;
}

.module-icon {
  width: 30px;
  height: 30px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  font-size: 16px;
  margin-bottom: 8px;
  background: #f1f5f9;
}

.module-title {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
}

.module-desc {
  margin: 5px 0 0;
  font-size: 12px;
  color: #64748b;
  line-height: 1.5;
}

.module-meta {
  margin-top: 8px;
  display: inline-flex;
  border-radius: 999px;
  font-size: 11px;
  padding: 4px 8px;
  background: #f8fafc;
  color: #475569;
  border: 1px solid #e2e8f0;
}

.split {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 10px;
}

.todo-list {
  display: grid;
  gap: 8px;
}

.todo-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 10px;
  background: #fff;
}

.todo-item .left {
  min-width: 0;
}

.todo-item .title {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
}

.todo-item .desc {
  margin-top: 2px;
  font-size: 11px;
  color: #64748b;
}

.badge {
  border-radius: 999px;
  font-size: 11px;
  padding: 4px 8px;
  white-space: nowrap;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  color: #334155;
}

@media (max-width: 768px) {
  .module-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .split {
    grid-template-columns: 1fr;
  }
}
</style>
