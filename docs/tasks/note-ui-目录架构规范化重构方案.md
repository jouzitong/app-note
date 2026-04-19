# note-ui 目录架构规范化重构方案

- 文档日期：2026-04-19
- 适用范围：`note-ui`（Vue2）
- 依据规范：`docs/spec/前端开发规范-目录架构设计.adoc`
- 目标：将 `note-ui` 从“可运行”提升为“结构清晰、边界可执行、可持续演进”的工程化前端项目

## 1. 现状分析（对照规范）

### 1.1 目录结构与业务域骨架

现状：
1. 页面按 `views/commons`、`views/modules`、`views/test` 混合组织，未形成统一 `views/domain/<domain>/pages|components|services|types|constants|__tests__` 骨架。
2. `components` 下同时存在业务组件和偏通用组件，边界不清。
3. 缺失 domain 级 `services` 与 `types` 目录，页面与组件直接承载业务编排逻辑。

证据：
1. `note-ui/src/views/commons/notes/index.vue`
2. `note-ui/src/views/modules/language-jp/home.vue`
3. `note-ui/src/components/word/WordCard.vue`

### 1.2 依赖边界可执行化

现状：
1. `package.json` 未安装 `eslint-plugin-import`，缺少 `no-restricted-imports` 边界规则。
2. 无 CI 级 lint/test 阻断约束（仅本地脚本 `lint`）。
3. 组件/页面直接依赖 `api`，未通过 domain service 抽象。

证据：
1. `note-ui/package.json`
2. `note-ui/src/views/LoginView.vue:43`
3. `note-ui/src/views/commons/practice/index.vue:149`
4. `note-ui/src/components/article/ArticleReader.vue:113`
5. `note-ui/src/components/word/WordCard.vue:285`

### 1.3 接口契约（API -> Mapper -> ViewModel）

现状：
1. `api` 层承担了大量“结果兼容/兜底”逻辑，但没有明确 DTO 与 ViewModel 的分层。
2. `mapper` 目录不存在，页面与组件直接吃 API 结果并二次处理。
3. `model` 目录未按 `model/api`、`model/views` 分层。

证据：
1. `note-ui/src/api/noteNodes.js:27`
2. `note-ui/src/api/noteNodes.js:57`
3. `note-ui/src/api/practices.js:9`
4. `note-ui/src/views/commons/notes/index.vue:221`
5. `note-ui/src/model/article/article.js`

### 1.4 路由治理

现状：
1. `test` 路由直接注册在正式路由表，未按环境隔离。
2. 路由主要为同步静态 import，缺少统一懒加载和 chunk 管理。
3. `meta` 字段使用 `public`，未统一 `requiresAuth/permissions/keepAlive/featureFlag`。
4. 缺失 404 与异常页兜底路由。

证据：
1. `note-ui/src/router/index.js:9`
2. `note-ui/src/router/index.js:96`
3. `note-ui/src/router/index.js:23`
4. `note-ui/src/router/index.js:82`

### 1.5 状态管理（Vuex）

现状：
1. 仅有单一根 `store`，`modules` 为空。
2. 未按业务域拆分命名空间模块。
3. 除 `globalEnums` 外，页面状态多以内聚在组件中，不利于复用和测试。

证据：
1. `note-ui/src/store/index.js:9`
2. `note-ui/src/store/index.js:100`

### 1.6 测试体系

现状：
1. `src` 内无业务测试用例（`*.spec.*` / `*.test.*`）。
2. `package.json` 无 `test:unit`、`test:e2e` 脚本。

证据：
1. `note-ui/package.json`
2. 当前业务源码目录无测试文件。

### 1.7 样式与设计令牌

现状：
1. 样式大量硬编码颜色、字号、间距，缺少统一 token。
2. 无 `src/assets/styles/tokens/*` 体系。

证据：
1. `note-ui/src/App.vue:34`
2. `note-ui/src/views/LoginView.vue:104`
3. `note-ui/src/components/article/ArticleReader.vue:317`
4. `note-ui/src/views/commons/practice/index.vue:666`

### 1.8 可观测性与错误处理

现状：
1. 缺少统一错误码映射表与用户文案映射策略。
2. 缺少统一埋点字段规范与埋点实现入口。
3. 缺少 Sentry（或等价平台）前端异常采集接入。

证据：
1. `note-ui/src/utils/error.js`
2. `note-ui/src/components/article/ArticleReader.vue:191`

---

## 2. 重构目标（必须达成）

1. 目录规范化：核心业务域达到标准骨架，新增需求禁止继续落在旧目录。
2. 边界可执行化：ESLint + CI 能自动阻断越层依赖。
3. 接口契约清晰化：API 只负责 DTO，Mapper 统一转换为 ViewModel。
4. 路由可治理：测试路由默认不进生产，补齐 404/异常页与 meta 规范。
5. 状态可维护：Vuex 模块化，异步生命周期统一（`loading/error/empty`）。
6. 质量门禁：补齐核心链路单测，CI 强制执行。
7. 视觉一致性：引入设计令牌，减少硬编码。
8. 可观测性落地：错误码映射、埋点、异常采集统一入口。

---

## 3. 可执行重构方案（分阶段）

### 阶段 A（P0）：骨架与门禁先行（预计 2-3 天）

目标：先把“规范执行器”搭起来，避免新代码继续发散。

任务：
1. 新增目录骨架（以 `language-jp` 为首个 domain）：
   1. `src/views/domain/language-jp/pages`
   2. `src/views/domain/language-jp/components`
   3. `src/views/domain/language-jp/services`
   4. `src/views/domain/language-jp/types`
   5. `src/views/domain/language-jp/constants`
   6. `src/views/domain/language-jp/__tests__`
2. 新增 `src/model/api`、`src/model/views` 目录。
3. 新增 `src/mappers/domain/language-jp` 目录。
4. 安装并配置 lint 规则：
   1. `eslint-plugin-import`
   2. `import/no-cycle`
   3. `no-restricted-imports`
5. 在 CI 中增加前端门禁脚本（至少 `npm run lint`）。

产出文件（新增/修改）：
1. `note-ui/package.json`
2. `note-ui/.eslintrc.js`（或迁移 `eslintConfig` 到独立文件）
3. `note-ui/src/views/domain/language-jp/**`
4. `note-ui/src/model/api/**`
5. `note-ui/src/model/views/**`
6. `note-ui/src/mappers/domain/language-jp/**`

验收标准：
1. 新目录创建完成并可跑通构建。
2. 越层 import 在本地 lint 可被阻断。
3. CI 对 lint 失败可阻断合并。

### 阶段 B（P0）：路由治理与 test 隔离（预计 1-2 天）

目标：清理最直接的线上风险点。

任务：
1. 路由拆分为：
   1. `src/router/base.js`
   2. `src/router/test.js`
   3. `src/router/index.js`（环境开关聚合）
2. 迁移 `/test/*` 路由到 `test.js`，默认生产禁用。
3. 统一路由 `meta` 字段：`title/requiresAuth/permissions/keepAlive/featureFlag`。
4. 补齐 `404` 与 `ErrorPage` 页面和兜底路由。
5. 将主业务路由改为懒加载模板（统一 chunkName 约定）。

产出文件：
1. `note-ui/src/router/base.js`
2. `note-ui/src/router/test.js`
3. `note-ui/src/router/index.js`
4. `note-ui/src/views/errors/NotFoundPage.vue`
5. `note-ui/src/views/errors/ErrorPage.vue`

验收标准：
1. 生产环境默认不可访问 `/test/*`。
2. 未匹配路径稳定进入 404。
3. 路由定义均包含标准化 `meta` 字段。

### 阶段 C（P0）：接口契约改造（预计 4-6 天）

目标：建立 `API DTO -> Mapper -> ViewModel` 主通道。

任务：
1. 先改造三条高价值链路：
   1. notes（详情/编辑）
   2. word-card
   3. article
2. `api/*` 仅返回 DTO（移除页面语义兼容逻辑）。
3. 新增 mapper：
   1. `noteNode.mapper.js`
   2. `wordCard.mapper.js`
   3. `article.mapper.js`
4. 页面/组件只调用 `services/*`，不直接调用 `api/*`。
5. 将现有 `model/*` 按职责拆分到 `model/api` 与 `model/views`。

产出文件：
1. `note-ui/src/api/*.js`（收敛）
2. `note-ui/src/mappers/domain/language-jp/*.js`
3. `note-ui/src/views/domain/language-jp/services/*.js`
4. `note-ui/src/model/api/**/*.js`
5. `note-ui/src/model/views/**/*.js`
6. 相关页面/组件导入路径更新。

验收标准：
1. `views/components` 中不再直接 import `@/api/*`。
2. mapper 函数均为纯函数（无请求/无副作用）。
3. 三条链路行为与改造前一致。

### 阶段 D（P1）：状态管理模块化（预计 2-4 天）

目标：把页面散落状态收敛到 Vuex 模块。

任务：
1. 新建模块：
   1. `store/modules/app.js`
   2. `store/modules/languageJpNotes.js`
   3. `store/modules/languageJpPractice.js`
2. 全模块 `namespaced: true`。
3. 统一 mutation/action 命名和职责。
4. 异步请求统一维护 `loading/error/empty`。

产出文件：
1. `note-ui/src/store/index.js`
2. `note-ui/src/store/modules/*.js`

验收标准：
1. 根 store 仅做模块聚合。
2. 关键页面不再维护重复的请求生命周期状态。

### 阶段 E（P1）：测试与可观测性（预计 3-5 天）

目标：建立可回归质量基线。

任务：
1. 引入单测框架（Vue2 建议 `@vue/test-utils` + `jest`）。
2. 增加脚本：`test:unit`、`test:unit:watch`。
3. 首批覆盖：
   1. 鉴权：登录成功/失败/重定向
   2. 下单替代链路（本项目对应练习提交链路）：`practice submit`
   3. 关键学习链路：word-card 翻页与完成状态
4. 新增错误码映射表：`src/constants/error-map.js`。
5. 错误处理统一走映射（`utils/error.js` 改造）。
6. 接入 Sentry（或预留统一上报适配层）。

产出文件：
1. `note-ui/package.json`
2. `note-ui/jest.config.js`（或等价配置）
3. `note-ui/src/constants/error-map.js`
4. `note-ui/src/utils/error.js`
5. `note-ui/src/observability/*`
6. `note-ui/src/**/__tests__/*.spec.js`

验收标准：
1. `npm run test:unit` 稳定通过。
2. 核心链路有可执行测试。
3. 异常能统一产出用户文案与错误上报。

### 阶段 F（P1）：样式 token 化与脚手架（预计 2-3 天）

目标：降低视觉碎片化和新增成本。

任务：
1. 建立 token 目录：
   1. `src/assets/styles/tokens/color.scss`
   2. `src/assets/styles/tokens/spacing.scss`
   3. `src/assets/styles/tokens/typography.scss`
2. 将高频公共样式（toast/tag/button/input）改为 token 引用。
3. 增加脚手架命令：`npm run gen:domain -- --name <domain>`。

产出文件：
1. `note-ui/src/assets/styles/tokens/*`
2. `note-ui/src/App.vue`（公共样式替换）
3. `note-ui/scripts/gen-domain.js`
4. `note-ui/package.json`

验收标准：
1. 新增页面不再硬编码核心颜色/字号/间距。
2. 可以一条命令生成 domain 骨架。

---

## 4. PR 拆分建议（降低风险）

1. PR-1：lint 规则与目录骨架（不改业务行为）。
2. PR-2：路由拆分 + test 隔离 + 404。
3. PR-3：notes 链路 API/mapper/service 改造。
4. PR-4：word-card 链路改造。
5. PR-5：article/practice 链路改造。
6. PR-6：Vuex 模块化。
7. PR-7：单测与 CI 门禁。
8. PR-8：样式 token 与脚手架。

每个 PR 要求：
1. 仅聚焦一个主题，禁止混入无关功能。
2. 通过 `lint + build + 对应链路手工回归`。
3. 若涉及路由/结构迁移，提供兼容 alias 或迁移说明。

---

## 5. 回滚与兼容策略

1. 路由迁移阶段保留旧 path alias，避免外部链接失效。
2. API 改造先“新增 mapper/service”，再替换调用点，最后清理旧逻辑。
3. 对高风险页面采用 feature flag（必要时）灰度切换。
4. 任一阶段出现线上风险，优先回滚最近单个 PR，而非整体回退。

---

## 6. 执行顺序（建议）

1. 先做阶段 A + B（把工程约束和线上风险先压住）。
2. 再做阶段 C（接口契约）作为主改造。
3. 然后做阶段 D + E（状态与测试）。
4. 最后做阶段 F（token 与脚手架）作为工程收口。

---

## 7. 启动清单（本周可直接开工）

1. 创建 `src/views/domain/language-jp` 骨架并迁移 `notes` 页面。
2. 拆分 `router/base.js` 与 `router/test.js`，将 `/test/*` 移出生产。
3. 引入 `eslint-plugin-import` 并添加 `no-restricted-imports`。
4. 建立 `mappers/domain/language-jp/noteNode.mapper.js`，改造 notes 链路。
5. 在 CI 增加 `note-ui` 前端 `lint` 任务。

