<template>
  <div class="mobile-page">
    <main class="content-scroll">
      <router-view />
    </main>

    <BottomNav
      v-if="showBottomNav"
      :tabs="tabs"
      :active-key="currentTabKey"
      :bounded="true"
      @select="handleTab"
    />
  </div>
</template>

<script>
import BottomNav from "@/components/navigation/BottomNav.vue";
import {
  buildLanguageJpMaterialsPath,
  getLastLanguageJpNoteId,
} from "@/utils/languageJpNav";
import { getLastLanguageJpPracticeNodeId } from "@/utils/languageJpPracticeNav";

export default {
  name: "LanguageJpLayout",
  components: {
    BottomNav,
  },
  data() {
    return {
      tabs: [
        {
          key: "home",
          label: "首页",
          icon: "⌂",
          to: { name: "language-jp-home" },
          matchNames: ["language-jp-home"],
        },
        {
          key: "exam",
          label: "考试",
          icon: "✎",
          action: "openPracticeModule",
          matchNames: ["language-jp-practice"],
        },
        {
          key: "course",
          label: "资料",
          icon: "▦",
          action: "openNoteModule",
          matchNames: [
            "language-jp-materials",
            "language-jp-materials-new",
            "language-jp-materials-edit",
          ],
        },
        {
          key: "plan",
          label: "计划",
          icon: "◷",
          action: "openWordCardModule",
          matchNames: ["language-jp-word-card"],
        },
        {
          key: "mine",
          label: "我的",
          icon: "◉",
          action: "",
        },
      ],
    };
  },
  computed: {
    showBottomNav() {
      return !this.$route.matched.some((record) => record.meta?.hideBottomNav);
    },
    currentTabKey() {
      const routeName = this.$route.name;
      const matched = this.tabs.find((tab) => {
        if (
          Array.isArray(tab.matchNames) &&
          tab.matchNames.includes(routeName)
        ) {
          return true;
        }
        if (tab.to && tab.to.name === routeName) {
          return true;
        }
        return false;
      });

      if (matched) {
        return matched.key;
      }
      return "home";
    },
  },
  methods: {
    navigateTo(target) {
      const targetRoute = this.$router.resolve(target).route;
      if (targetRoute.fullPath === this.$route.fullPath) {
        return;
      }
      this.$router.push(target);
    },
    openNoteModule() {
      this.navigateTo(buildLanguageJpMaterialsPath());
    },
    openWordCardModule() {
      const noteId = getLastLanguageJpNoteId();
      this.navigateTo({
        name: "language-jp-word-card",
        params: { parentId: String(noteId) },
        query: { nodeId: String(noteId), pageIndex: "1", wordIndex: "0" },
      });
    },
    openArticleModule() {
      const noteId = getLastLanguageJpNoteId();
      this.navigateTo({
        name: "language-jp-article",
        params: { parentId: String(noteId) },
        query: { nodeId: String(noteId) },
      });
    },
    openPracticeModule() {
      const parentId = getLastLanguageJpNoteId();
      const nodeId = getLastLanguageJpPracticeNodeId(parentId);
      this.navigateTo({
        name: "language-jp-practice",
        params: { parentId: String(parentId) },
        query: { nodeId: String(nodeId) },
      });
    },
    handleTab(tab) {
      if (tab.to) {
        this.navigateTo(tab.to);
        return;
      }

      if (!tab.action) {
        window.alert("该功能暂未实现。");
        return;
      }

      const handler = this[tab.action];
      if (typeof handler !== "function") {
        window.alert("该功能暂未实现。");
        return;
      }
      handler();
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

@media (min-width: 768px) {
  .mobile-page {
    max-width: 430px;
    margin: 0 auto;
    border-left: 1px solid #e5e7eb;
    border-right: 1px solid #e5e7eb;
  }
}
</style>
