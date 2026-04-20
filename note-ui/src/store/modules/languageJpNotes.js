export default {
  namespaced: true,
  state: () => ({
    detail: null,
    loading: false,
    error: "",
    empty: true,
  }),
  getters: {
    hasDetail(state) {
      return !!state.detail;
    },
  },
  mutations: {
    SET_LOADING(state, loading) {
      state.loading = !!loading;
    },
    SET_ERROR(state, error) {
      state.error = error || "";
    },
    SET_DETAIL(state, detail) {
      state.detail = detail || null;
      state.empty = !state.detail;
    },
    RESET(state) {
      state.detail = null;
      state.loading = false;
      state.error = "";
      state.empty = true;
    },
  },
  actions: {},
};
