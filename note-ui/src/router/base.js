const HomeView = () =>
  import(/* webpackChunkName: "home" */ "../views/HomeView.vue");
const LanguageJpView = () =>
  import(
    /* webpackChunkName: "domain-language-jp" */ "../views/modules/language-jp/layout.vue"
  );
const LanguageJpHomeView = () =>
  import(
    /* webpackChunkName: "domain-language-jp" */ "../views/modules/language-jp/home.vue"
  );
const LoginView = () =>
  import(/* webpackChunkName: "auth" */ "../views/LoginView.vue");
const AboutView = () =>
  import(/* webpackChunkName: "about" */ "../views/AboutView.vue");
const NoteIndexView = () =>
  import(
    /* webpackChunkName: "domain-language-jp-notes" */ "../views/commons/notes/index.vue"
  );
const NoteEditView = () =>
  import(
    /* webpackChunkName: "domain-language-jp-notes" */ "../views/commons/notes/edit.vue"
  );
const WordCardIndexView = () =>
  import(
    /* webpackChunkName: "domain-language-jp-word-card" */ "../views/commons/wordCard/index.vue"
  );
const ArticleReaderIndexView = () =>
  import(
    /* webpackChunkName: "domain-language-jp-article" */ "../views/commons/article/index.vue"
  );
const PracticeExamIndexView = () =>
  import(
    /* webpackChunkName: "domain-language-jp-practice" */ "../views/commons/practice/index.vue"
  );
const ErrorPage = () =>
  import(/* webpackChunkName: "error-pages" */ "../views/errors/ErrorPage.vue");
const NotFoundPage = () =>
  import(
    /* webpackChunkName: "error-pages" */ "../views/errors/NotFoundPage.vue"
  );

export default [
  {
    path: "/",
    name: "note-home",
    component: HomeView,
    meta: {
      title: "首页",
      requiresAuth: false,
      permissions: [],
      keepAlive: false,
    },
  },
  {
    path: "/language-jp",
    component: LanguageJpView,
    meta: {
      title: "日语学习",
      requiresAuth: true,
      permissions: [],
      keepAlive: true,
    },
    children: [
      {
        path: "",
        name: "language-jp-home",
        component: LanguageJpHomeView,
        meta: {
          title: "日语学习首页",
          requiresAuth: false,
          permissions: [],
          keepAlive: true,
        },
      },
      {
        path: "materials/new",
        alias: ["/language-jp/note/new", "/note/new"],
        name: "language-jp-materials-new",
        component: NoteEditView,
        meta: {
          title: "新增节点",
          requiresAuth: true,
          permissions: ["note:write"],
          keepAlive: false,
        },
      },
      {
        path: "materials/:id?",
        alias: ["/language-jp/note/:id?", "/note/:id?"],
        name: "language-jp-materials",
        component: NoteIndexView,
        meta: {
          title: "节点详情",
          requiresAuth: true,
          permissions: ["note:read"],
          keepAlive: true,
        },
      },
      {
        path: "materials/:id/edit",
        alias: ["/language-jp/note/:id/edit", "/note/:id/edit"],
        name: "language-jp-materials-edit",
        component: NoteEditView,
        meta: {
          title: "编辑节点",
          requiresAuth: true,
          permissions: ["note:write"],
          keepAlive: false,
        },
      },
      {
        path: "article/:parentId",
        alias: [
          "/language-jp/note/:parentId/article",
          "/note/:parentId/article",
        ],
        name: "language-jp-article",
        component: ArticleReaderIndexView,
        meta: {
          title: "文章阅读",
          requiresAuth: true,
          permissions: ["article:read"],
          keepAlive: true,
        },
      },
      {
        path: "practice/:parentId",
        alias: [
          "/language-jp/note/:parentId/practice",
          "/note/:parentId/practice",
        ],
        name: "language-jp-practice",
        component: PracticeExamIndexView,
        meta: {
          title: "练习",
          requiresAuth: true,
          permissions: ["practice:read"],
          keepAlive: false,
          featureFlag: "practice",
        },
      },
    ],
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
    meta: {
      title: "单词卡",
      requiresAuth: true,
      permissions: ["word-card:read"],
      keepAlive: true,
    },
  },
  {
    path: "/login",
    name: "login",
    component: LoginView,
    meta: {
      title: "登录",
      requiresAuth: false,
      permissions: [],
      keepAlive: false,
    },
  },
  {
    path: "/about",
    name: "about",
    component: AboutView,
    meta: {
      title: "关于",
      requiresAuth: false,
      permissions: [],
      keepAlive: false,
    },
  },
  {
    path: "/error",
    name: "error-page",
    component: ErrorPage,
    meta: {
      title: "系统异常",
      requiresAuth: false,
      permissions: [],
      keepAlive: false,
    },
  },
  {
    path: "*",
    name: "not-found",
    component: NotFoundPage,
    meta: {
      title: "页面未找到",
      requiresAuth: false,
      permissions: [],
      keepAlive: false,
    },
  },
];
