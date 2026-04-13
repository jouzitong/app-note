const { defineConfig } = require("@vue/cli-service");
const target = process.env.VUE_APP_API_PROXY_TARGET || "http://localhost:19812";

function normalizePrefix(raw) {
  if (!raw) {
    return "";
  }
  try {
    if (/^https?:\/\//i.test(raw)) {
      const url = new URL(raw);
      return (url.pathname || "").replace(/\/+$/, "");
    }
  } catch (error) {
    // ignore
  }
  const p = `${raw}`.trim();
  if (!p) {
    return "";
  }
  if (p === "/") {
    return "";
  }
  return p.startsWith("/")
    ? p.replace(/\/+$/, "")
    : `/${p}`.replace(/\/+$/, "");
}

const prefix = normalizePrefix(process.env.VUE_APP_API_BASE_URL || "/app-note");

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    proxy: {
      ...(prefix
        ? {
            [`${prefix}/api`]: { target, changeOrigin: true },
            [`${prefix}/auth`]: { target, changeOrigin: true },
          }
        : {}),
      "/api": { target, changeOrigin: true },
      "/auth": { target, changeOrigin: true },
    },
  },
});
