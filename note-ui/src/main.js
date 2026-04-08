import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";
import { hasAuthToken } from "@/utils/auth";

Vue.config.productionTip = false;

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
