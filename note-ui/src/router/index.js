import Vue from "vue";
import VueRouter from "vue-router";
import HomeView from "../views/HomeView.vue";
import LanguageJpView from "../views/modules/language-jp/layout.vue";
import LanguageJpHomeView from "../views/modules/language-jp/home.vue";
import LoginView from "../views/LoginView.vue";
import NoteIndexView from "../views/commons/notes/index.vue";
import NoteEditView from "../views/commons/notes/edit.vue";
import WordCardView from "@/views/test/word-card.vue";
import WordCardSpeechTestView from "@/views/test/word-card-speech.vue";
import WordCardIndexView from "@/views/commons/wordCard/index.vue";
import ArticleReaderIndexView from "@/views/commons/article/index.vue";
import PracticeExamIndexView from "@/views/commons/practice/index.vue";
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
        name: "language-jp-home",
        component: LanguageJpHomeView,
        meta: { public: true },
      },
      {
        path: "materials/new",
        alias: ["/language-jp/note/new", "/note/new"],
        name: "language-jp-materials-new",
        component: NoteEditView,
      },
      {
        path: "materials/:id?",
        alias: ["/language-jp/note/:id?", "/note/:id?"],
        name: "language-jp-materials",
        component: NoteIndexView,
      },
      {
        path: "materials/:id/edit",
        alias: ["/language-jp/note/:id/edit", "/note/:id/edit"],
        name: "language-jp-materials-edit",
        component: NoteEditView,
      },
      {
        path: "article/:parentId",
        alias: [
          "/language-jp/note/:parentId/article",
          "/note/:parentId/article",
        ],
        name: "language-jp-article",
        component: ArticleReaderIndexView,
      },
      {
        path: "practice/:parentId",
        alias: [
          "/language-jp/note/:parentId/practice",
          "/note/:parentId/practice",
        ],
        name: "language-jp-practice",
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
    path: "/language-jp/word-card/:parentId",
    alias: [
      "/language-jp/note/:parentId/word",
      "/note/:parentId/word",
      "/note/:parentId/word-card",
    ],
    name: "language-jp-word-card",
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
