import Vue from "vue";
import Vuex from "vuex";
import app from "./modules/app";
import languageJpNotes from "./modules/languageJpNotes";
import languageJpPractice from "./modules/languageJpPractice";

Vue.use(Vuex);

export default new Vuex.Store({
  modules: {
    app,
    languageJpNotes,
    languageJpPractice,
  },
});
