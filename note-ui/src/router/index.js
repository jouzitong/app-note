import Vue from "vue";
import VueRouter from "vue-router";
import baseRoutes from "./base";
import testRoutes from "./test";
import { hasAuthToken } from "@/utils/auth";

Vue.use(VueRouter);

const routes = [...baseRoutes];
const enableTestRoutes =
  process.env.NODE_ENV !== "production" &&
  process.env.VUE_APP_ENABLE_TEST_ROUTE === "true";

if (enableTestRoutes) {
  routes.push(...testRoutes);
}

const router = new VueRouter({
  mode: "history",
  base: process.env.BASE_URL,
  routes,
});

router.beforeEach((to, from, next) => {
  const requiresAuth = to.matched.some(
    (record) => record.meta?.requiresAuth === true
  );
  const loggedIn = hasAuthToken();

  if (to.name === "login" && loggedIn) {
    next({ name: "note-home" });
    return;
  }

  if (!requiresAuth || loggedIn) {
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
