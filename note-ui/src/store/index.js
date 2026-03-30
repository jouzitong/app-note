import Vue from "vue";
import Vuex from "vuex";
import { fetchGlobalEnums as requestGlobalEnums } from "@/api/enums";

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    globalEnums: {},
    globalEnumsLoading: false,
    globalEnumsLoaded: false,
    globalEnumsError: "",
  },
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
  },
  actions: {
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
  modules: {},
});
