import { fetchGlobalEnums as requestGlobalEnums } from "@/api/enums";

let toastTimer = null;

export default {
  namespaced: true,
  state: () => ({
    globalEnums: {},
    globalEnumsLoading: false,
    globalEnumsLoaded: false,
    globalEnumsError: "",
    toast: {
      visible: false,
      message: "",
      type: "error",
    },
  }),
  getters: {
    globalEnums(state) {
      return state.globalEnums;
    },
    enumByKey: (state) => (key) => {
      if (!key) {
        return [];
      }
      const raw = state.globalEnums?.[key];
      return Array.isArray(raw) ? raw : [];
    },
  },
  mutations: {
    SET_GLOBAL_ENUMS_LOADING(state, loading) {
      state.globalEnumsLoading = loading;
    },
    SET_GLOBAL_ENUMS_LOADED(state, loaded) {
      state.globalEnumsLoaded = loaded;
    },
    SET_GLOBAL_ENUMS_ERROR(state, errorMessage) {
      state.globalEnumsError = errorMessage || "";
    },
    SET_GLOBAL_ENUMS(state, enums) {
      state.globalEnums = enums && typeof enums === "object" ? enums : {};
    },
    SHOW_TOAST(state, payload = {}) {
      state.toast.visible = true;
      state.toast.message = (payload?.message || "").trim();
      state.toast.type = payload?.type || "error";
    },
    HIDE_TOAST(state) {
      state.toast.visible = false;
    },
  },
  actions: {
    showToast({ commit }, payload = {}) {
      const message = (payload?.message || "").trim();
      if (!message) {
        return;
      }
      commit("SHOW_TOAST", payload);
      if (toastTimer) {
        clearTimeout(toastTimer);
        toastTimer = null;
      }
      const durationMs =
        typeof payload?.durationMs === "number" ? payload.durationMs : 3000;
      const safeDuration = Number.isFinite(durationMs) ? durationMs : 3000;
      toastTimer = setTimeout(() => {
        commit("HIDE_TOAST");
        toastTimer = null;
      }, Math.max(0, safeDuration));
    },
    notifyError({ dispatch }, message) {
      return dispatch("showToast", { type: "error", message });
    },
    async fetchGlobalEnums({ state, commit }, options = {}) {
      const force = Boolean(options?.force);
      if (state.globalEnumsLoading) {
        return state.globalEnums;
      }
      if (state.globalEnumsLoaded && !force) {
        return state.globalEnums;
      }

      commit("SET_GLOBAL_ENUMS_LOADING", true);
      commit("SET_GLOBAL_ENUMS_ERROR", "");
      try {
        const enums = await requestGlobalEnums();
        commit("SET_GLOBAL_ENUMS", enums);
        commit("SET_GLOBAL_ENUMS_LOADED", true);
        return enums;
      } catch (error) {
        commit("SET_GLOBAL_ENUMS_ERROR", error?.message || "加载全局枚举失败");
        throw error;
      } finally {
        commit("SET_GLOBAL_ENUMS_LOADING", false);
      }
    },
  },
};
