const WordCardView = () =>
  import(/* webpackChunkName: "test-routes" */ "@/views/test/word-card.vue");
const WordCardSpeechTestView = () =>
  import(
    /* webpackChunkName: "test-routes" */ "@/views/test/word-card-speech.vue"
  );

export default [
  {
    path: "/test/word-card",
    name: "test-word-card",
    component: WordCardView,
    meta: {
      title: "测试-单词卡",
      requiresAuth: false,
      permissions: [],
      keepAlive: false,
      featureFlag: "test-route",
    },
  },
  {
    path: "/test/word-card-speech",
    name: "test-word-card-speech",
    component: WordCardSpeechTestView,
    meta: {
      title: "测试-语音转写",
      requiresAuth: false,
      permissions: [],
      keepAlive: false,
      featureFlag: "test-route",
    },
  },
];
