import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";
import { hasAuthToken } from "@/utils/auth";
import { configureHttpHooks } from "@/utils/http";
import { getErrorMessage } from "@/utils/error";

Vue.config.productionTip = false;

configureHttpHooks({
  onError(error, ctx = {}) {
    const options = ctx?.options || {};
    if (options?.silentError === true) {
      return;
    }
    if (error?.status === 401 && options?.redirectOn401 !== false) {
      return;
    }
    const message = getErrorMessage(error) || "请求失败，请稍后重试";
    store.dispatch("notifyError", message).catch(() => {});
  },
});

if (hasAuthToken()) {
  store.dispatch("fetchGlobalEnums").catch((error) => {
    console.error("[store] fetchGlobalEnums failed:", error);
  });
}

new Vue({
  router,
  store,
  render: (h) => h(App),
}).$mount("#app");
