import Vue from "vue";
import VueRouter from "vue-router";
import HomeView from "../views/HomeView.vue";
import NoteIndexView from "../views/notes/index.vue";
import NoteEditView from "../views/notes/edit.vue";
import WordCardView from "@/views/test/word-card.vue";
import WordCardIndexView from "@/views/wordCard/index.vue";
import ArticleReaderIndexView from "@/views/article/index.vue";

Vue.use(VueRouter);

const routes = [
  {
    path: "/",
    name: "home",
    component: HomeView,
  },
  {
    path: "/about",
    name: "about",
    // route level code-splitting
    // this generates a separate chunk (about.[hash].js) for this route
    // which is lazy-loaded when the route is visited.
    component: () =>
      import(/* webpackChunkName: "about" */ "../views/AboutView.vue"),
  },
  {
    path: "/note/:id?",
    name: "note",
    component: NoteIndexView,
  },
  {
    path: "/note/:id/edit",
    name: "note-edit",
    component: NoteEditView,
  },
  {
    path: "/test/word-card",
    name: "test-word-card",
    component: WordCardView,
  },
  {
    path: "/note/:parentId/word-card",
    name: "word-card",
    component: WordCardIndexView,
  },
  {
    path: "/note/:parentId/article",
    name: "article-reader",
    component: ArticleReaderIndexView,
  },
];

const router = new VueRouter({
  mode: "history",
  base: process.env.BASE_URL,
  routes,
});

export default router;
