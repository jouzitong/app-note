import Vue from "vue";
import VueRouter from "vue-router";
import HomeView from "../views/HomeView.vue";
import LanguageJpView from "../views/language-jp.vue";
import LanguageJpHomeView from "../views/language-jp-home.vue";
import LoginView from "../views/LoginView.vue";
import NoteIndexView from "../views/notes/index.vue";
import NoteEditView from "../views/notes/edit.vue";
import WordCardView from "@/views/test/word-card.vue";
import WordCardSpeechTestView from "@/views/test/word-card-speech.vue";
import WordCardIndexView from "@/views/wordCard/index.vue";
import ArticleReaderIndexView from "@/views/article/index.vue";
import PracticeExamIndexView from "@/views/practice/index.vue";
import { hasAuthToken } from "@/utils/auth";

Vue.use(VueRouter);

const routes = [
  {
    path: "/",
    name: "note-home",
    component: HomeView,
    meta: { public: true },
  },
  {
    path: "/language-jp",
    component: LanguageJpView,
    children: [
      {
        path: "",
        name: "language-jp",
        component: LanguageJpHomeView,
        meta: { public: true },
      },
      {
        path: "note/:id?",
        alias: ["/note/:id?"],
        name: "note",
        component: NoteIndexView,
      },
      {
        path: "note/:id/edit",
        alias: ["/note/:id/edit"],
        name: "note-edit",
        component: NoteEditView,
      },
      {
        path: "note/:parentId/article",
        alias: ["/note/:parentId/article"],
        name: "article-reader",
        component: ArticleReaderIndexView,
      },
      {
        path: "note/:parentId/practice",
        alias: ["/note/:parentId/practice"],
        name: "practice-exam",
        component: PracticeExamIndexView,
      },
    ],
  },
  {
    path: "/login",
    name: "login",
    component: LoginView,
    meta: { public: true },
  },
  {
    path: "/about",
    name: "about",
    component: () => import("../views/AboutView.vue"),
    meta: { public: true },
  },
  {
    path: "/language-jp/note/:parentId/word",
    alias: ["/note/:parentId/word", "/note/:parentId/word-card"],
    name: "word-card",
    component: WordCardIndexView,
  },
  {
    path: "/test/word-card",
    name: "test-word-card",
    component: WordCardView,
  },
  {
    path: "/test/word-card-speech",
    name: "test-word-card-speech",
    component: WordCardSpeechTestView,
  },
];

const router = new VueRouter({
  mode: "history",
  base: process.env.BASE_URL,
  routes,
});

router.beforeEach((to, from, next) => {
  const isPublic = to.matched.some((record) => record.meta?.public);
  const loggedIn = hasAuthToken();

  if (to.name === "login" && loggedIn) {
    next({ name: "note-home" });
    return;
  }

  if (isPublic || loggedIn) {
    next();
    return;
  }

  next({
    name: "login",
    query: {
      redirect: to.fullPath,
    },
  });
});

export default router;
