#!/usr/bin/env node

const fs = require("fs");
const path = require("path");

function parseArgs(argv) {
  const args = { name: "" };
  for (let i = 2; i < argv.length; i += 1) {
    const item = argv[i];
    if (item === "--name") {
      args.name = argv[i + 1] || "";
      i += 1;
      continue;
    }
    if (item.startsWith("--name=")) {
      args.name = item.split("=")[1] || "";
    }
  }
  return args;
}

function toKebabCase(input) {
  return String(input || "")
    .trim()
    .replace(/([a-z0-9])([A-Z])/g, "$1-$2")
    .replace(/[_\s]+/g, "-")
    .replace(/[^a-zA-Z0-9-]/g, "-")
    .replace(/-+/g, "-")
    .replace(/^-|-$/g, "")
    .toLowerCase();
}

function toPascalCase(input) {
  return String(input || "")
    .split("-")
    .filter(Boolean)
    .map((part) => part[0].toUpperCase() + part.slice(1))
    .join("");
}

function ensureDir(dirPath) {
  fs.mkdirSync(dirPath, { recursive: true });
}

function writeIfMissing(filePath, content) {
  if (fs.existsSync(filePath)) {
    return false;
  }
  fs.writeFileSync(filePath, content, "utf8");
  return true;
}

function run() {
  const args = parseArgs(process.argv);
  const domain = toKebabCase(args.name);
  if (!domain) {
    console.error("[gen:domain] Missing --name, e.g. npm run gen:domain -- --name order");
    process.exit(1);
  }

  const root = process.cwd();
  const pascal = toPascalCase(domain);

  const viewRoot = path.join(root, "src/views/domain", domain);
  const modelRoot = path.join(root, "src/model/views/domain", domain);
  const routerRoot = path.join(root, "src/router/modules");

  const dirs = [
    path.join(viewRoot, "pages"),
    path.join(viewRoot, "components"),
    path.join(viewRoot, "services"),
    path.join(viewRoot, "types"),
    path.join(viewRoot, "constants"),
    path.join(viewRoot, "__tests__"),
    modelRoot,
    routerRoot,
  ];
  dirs.forEach(ensureDir);

  const created = [];

  const pageFile = path.join(viewRoot, "pages", `${pascal}HomePage.vue`);
  if (
    writeIfMissing(
      pageFile,
      `<template>\n  <section class="${domain}-home-page">\n    <h1>${pascal} Home</h1>\n  </section>\n</template>\n\n<script>\nexport default {\n  name: "${pascal}HomePage",\n};\n</script>\n\n<style scoped>\n.${domain}-home-page {\n  min-height: 100vh;\n  padding: var(--space-5);\n  background: var(--color-surface-page);\n}\n</style>\n`
    )
  ) {
    created.push(path.relative(root, pageFile));
  }

  const serviceFile = path.join(viewRoot, "services", `${domain}.service.js`);
  if (
    writeIfMissing(
      serviceFile,
      `export async function fetch${pascal}Overview() {\n  return {\n    items: [],\n  };\n}\n`
    )
  ) {
    created.push(path.relative(root, serviceFile));
  }

  const typeFile = path.join(viewRoot, "types", `${domain}.types.js`);
  if (
    writeIfMissing(
      typeFile,
      `export function createDefault${pascal}Item() {\n  return {\n    id: null,\n    name: "",\n  };\n}\n`
    )
  ) {
    created.push(path.relative(root, typeFile));
  }

  const constantFile = path.join(viewRoot, "constants", `${domain}.constants.js`);
  if (
    writeIfMissing(
      constantFile,
      `export const ${domain.toUpperCase().replace(/-/g, "_")}_FEATURE_FLAG = "${domain}";\n`
    )
  ) {
    created.push(path.relative(root, constantFile));
  }

  const testFile = path.join(viewRoot, "__tests__", `${domain}.service.spec.js`);
  if (
    writeIfMissing(
      testFile,
      `import { fetch${pascal}Overview } from "@/views/domain/${domain}/services/${domain}.service";\n\ndescribe("${domain}.service", () => {\n  test("fetch${pascal}Overview returns default shape", async () => {\n    const result = await fetch${pascal}Overview();\n    expect(Array.isArray(result.items)).toBe(true);\n  });\n});\n`
    )
  ) {
    created.push(path.relative(root, testFile));
  }

  const stateFile = path.join(modelRoot, `${domain}.state.js`);
  if (
    writeIfMissing(
      stateFile,
      `export function createDefault${pascal}PageState() {\n  return {\n    loading: false,\n    error: "",\n    empty: true,\n  };\n}\n`
    )
  ) {
    created.push(path.relative(root, stateFile));
  }

  const routeFile = path.join(routerRoot, `${domain}.js`);
  if (
    writeIfMissing(
      routeFile,
      `const ${pascal}HomePage = () =>\n  import(/* webpackChunkName: "domain-${domain}" */ "@/views/domain/${domain}/pages/${pascal}HomePage.vue");\n\nexport default [\n  {\n    name: "${domain}-home",\n    path: "/${domain}",\n    component: ${pascal}HomePage,\n    meta: {\n      title: "${pascal}",\n      requiresAuth: true,\n      permissions: [],\n      keepAlive: true,\n    },\n  },\n];\n`
    )
  ) {
    created.push(path.relative(root, routeFile));
  }

  const readmeFile = path.join(viewRoot, "README.md");
  if (
    writeIfMissing(
      readmeFile,
      `# ${domain} domain\n\n- pages: 路由页面\n- components: 域内复用组件\n- services: 业务编排\n- types: 前端语义类型\n- constants: 域内常量\n- __tests__: 域内测试\n`
    )
  ) {
    created.push(path.relative(root, readmeFile));
  }

  console.log(`[gen:domain] Domain '${domain}' scaffolding finished.`);
  if (!created.length) {
    console.log("[gen:domain] No files created (all files already exist).");
    return;
  }
  created.forEach((item) => console.log(`  + ${item}`));
}

run();
