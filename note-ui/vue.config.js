const { defineConfig } = require("@vue/cli-service");
const target =
  process.env.VUE_APP_API_PROXY_TARGET ||
  process.env.VUE_APP_API_BASE_URL ||
  "http://localhost:19812";

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    proxy: {
      "/api": {
        target,
        changeOrigin: true,
      },
      "/auth": {
        target,
        changeOrigin: true,
      },
    },
  },
});
