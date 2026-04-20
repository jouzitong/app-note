const LanguageJpHomePage = () =>
  import(
    /* webpackChunkName: "domain-language-jp" */ "@/views/domain/language-jp/pages/LanguageJpHomePage.vue"
  );

export default [
  {
    name: "language-jp-home",
    path: "/language-jp",
    component: LanguageJpHomePage,
    meta: {
      title: "LanguageJp",
      requiresAuth: true,
      permissions: [],
      keepAlive: true,
    },
  },
];
