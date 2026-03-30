import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";

Vue.config.productionTip = false;

store.dispatch("fetchGlobalEnums").catch((error) => {
  // 全局枚举加载失败不阻断首屏渲染，页面内按需兜底。
  console.error("[store] fetchGlobalEnums failed:", error);
});

new Vue({
  router,
  store,
  render: (h) => h(App),
}).$mount("#app");
