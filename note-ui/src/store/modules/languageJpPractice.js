export default {
  namespaced: true,
  state: () => ({
    session: null,
    question: null,
    loading: false,
    error: "",
    empty: true,
  }),
  getters: {
    hasSession(state) {
      return !!state.session;
    },
  },
  mutations: {
    SET_LOADING(state, loading) {
      state.loading = !!loading;
    },
    SET_ERROR(state, error) {
      state.error = error || "";
    },
    SET_SESSION(state, session) {
      state.session = session || null;
      state.empty = !state.session;
    },
    SET_QUESTION(state, question) {
      state.question = question || null;
    },
    RESET(state) {
      state.session = null;
      state.question = null;
      state.loading = false;
      state.error = "";
      state.empty = true;
    },
  },
  actions: {},
};
