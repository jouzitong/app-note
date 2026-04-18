# app-layer 按 NoteType 动态返回内容方案

- 文档日期：2026-04-18
- 适用范围：`app-layer`（主），联动 `data-core` / `server-word` / `server-practice` / `note-ui`
- 目标：在 app-layer 提供一个聚合接口，按 `NoteNode.noteType` 返回不同业务对象给前端

## 1. 我对当前代码的理解

### 1.1 当前分层

1. `data-core` 负责笔记节点主模型（`NoteNode`）和通用内容（`NoteNodeContent`），通过 `NoteNodeDomainController` 暴露核心节点接口。
2. `module/server-word` 负责单词卡与文章等业务域，和 `NoteNode` 的关系通过关联表维护：
   1. 单词卡：`word_card_note_node_rel`
   2. 文章：`article_note_node_rel`
3. `module/server-practice` 负责题目域，题目和节点通过 `question_note_node_rel` 关联。
4. `app-layer` 当前仅有运维删除与导入接口，还没有“节点详情动态聚合”入口。

### 1.2 当前数据模型关键点

1. `NoteNode` 既是目录，也可承载普通内容（`note_node_content`）。
2. `noteType` 决定“该节点下真实业务内容在哪个模块”。
3. `data-core` 的 `NoteNodeVO` 已预留 `Object content` 动态槽位，但目前 `get` 只读取 `note_node_content`。
4. 词卡/文章/题目都已具备“按 noteNodeId 查询”的服务能力（或可直接复用分页接口）。

### 1.3 前端现状

1. `note-ui` 现在拿 `GET /api/v1/noteNodes/domain/{id}`，并使用 `content` 做动态渲染入口。
2. 前端已有按 `noteType` 的分支思路（如 `WORD_CARD` 跳转逻辑），说明前后端契约可以走“统一壳 + 动态 payload”。

## 2. 目标方案（可执行）

### 2.1 新增 app-layer 聚合接口

建议新增：

1. 路径：`GET /api/v1/app/noteNodes/{noteNodeId}/content`
2. 控制器：`app-layer/.../controller/NoteNodeContentAppController`
3. 服务：`app-layer/.../service/INoteNodeContentAppService`
4. 实现：`NoteNodeContentAppServiceImpl`

说明：

1. 该接口不替代 `data-core` 的 domain 接口，而是“面向前端聚合视图”的 app 层接口。
2. 前端可逐步迁移；旧接口保留。

### 2.2 返回结构（统一壳 + 动态对象）

建议定义 `NoteNodeContentAppVO`（示例字段）：

1. `noteNode`: `NoteNodeDTO`（节点基础信息）
2. `paths`: `List<NoteNodePathVO>`（路径）
3. `childNoteNodes`: `List<NoteNodePathVO>`（子节点）
4. `noteType`: `String`（冗余，便于前端直接分支）
5. `contentType`: `String`（例如 `NOTE_NODE_CONTENT` / `WORD_CARD_PAGE` / `ARTICLE_DETAIL` / `QUESTION_PAGE`）
6. `content`: `Object`（动态业务对象）
7. `ext`: `Map<String, Object>`（可选扩展，例如分页参数回显）

这样前端只需：先看 `contentType`，再按类型解析 `content`。

### 2.3 按 noteType 分发机制（核心）

在 app-layer 引入策略注册机制，避免 if-else 膨胀。

建议接口：

1. `INoteTypeContentResolver`
2. `boolean supports(NoteType noteType)`
3. `String contentType()`
4. `Object resolve(NoteNode noteNode, NoteNodeContentQuery query)`

首批 resolver：

1. `MarkdownNoteContentResolver`：返回 `note_node_content`（通用文本/JSON）。
2. `WordCardContentResolver`：调用 `IWordCardDomainService.page(noteId,page,size,userId)`。
3. `ArticleContentResolver`：调用 `IArticleDomainService.getByNoteNodeId(noteId)`。
4. `QuestionContentResolver`：调用 `IQuestionDomainService.page(request)`。
5. `EmptyContentResolver`：返回 `null` 或空对象。

app service 主流程：

1. 先通过 `INoteNodeDomainService.get` 获取节点壳信息。
2. 再根据 `noteType` 找到 resolver。
3. resolver 组装业务内容后回填统一 VO。

### 2.4 参数约定（兼容不同内容形态）

为避免后续重复开接口，建议在聚合接口支持以下 query 参数：

1. `page` / `size`：分页型内容（词卡、题目）
2. `index`：索引型内容（若未来保留“按序号取单条词卡”）
3. `userId`：个性化进度
4. `includeChildren`：是否返回子节点（默认 true）

建议新增请求对象：`NoteNodeContentQuery`。

### 2.5 错误与降级策略

1. 不支持类型：返回 `contentType=UNSUPPORTED`，`content=null`，并在 `ext.message` 标注类型未接入。
2. 业务数据为空：返回对应 `contentType` 但 `content` 为空集合或空对象（不抛 500）。
3. 仅节点不存在时抛参数错误（400）。

## 3. 分阶段实施计划

### 第 1 阶段：搭骨架（当天可完成）

1. 新建 app-layer controller/service/VO/query 对象。
2. 接入 `INoteNodeDomainService.get`，先透传节点基础信息。
3. 实现 `MARKDOWN/EMPTY` 两种 resolver。
4. 联调通过后先给前端开可用接口。

验收：

1. `GET /api/v1/app/noteNodes/{id}/content` 可返回统一结构。
2. markdown 节点内容与当前 domain 详情一致。

### 第 2 阶段：接入业务类型（1~2 天）

1. 接入 `WORD_CARD -> IWordCardDomainService.page`。
2. 接入 `ARTICLE -> IArticleDomainService.getByNoteNodeId`。
3. 接入 `QUESTIONS -> IQuestionDomainService.page`。
4. 补充每种类型的 `contentType` 常量。

验收：

1. 三类节点返回各自业务对象。
2. 分页参数可生效并回显。

### 第 3 阶段：前端切换（半天~1 天）

1. `note-ui` 新增 `getNoteNodeContentByType` API。
2. 页面优先请求 app-layer 聚合接口。
3. 前端按 `contentType` 渲染组件，保留旧接口兜底一段时间。

验收：

1. 同一路由下可按节点类型展示不同内容区域。
2. 不影响旧节点浏览流程。

## 4. 代码落点建议

### app-layer 新增

1. `app-layer/src/main/java/org/zzt/note/app/layer/content/controller/NoteNodeContentAppController.java`
2. `app-layer/src/main/java/org/zzt/note/app/layer/content/service/INoteNodeContentAppService.java`
3. `app-layer/src/main/java/org/zzt/note/app/layer/content/service/impl/NoteNodeContentAppServiceImpl.java`
4. `app-layer/src/main/java/org/zzt/note/app/layer/content/resolver/INoteTypeContentResolver.java`
5. `app-layer/src/main/java/org/zzt/note/app/layer/content/resolver/impl/*Resolver.java`
6. `app-layer/src/main/java/org/zzt/note/app/layer/content/vo/NoteNodeContentAppVO.java`
7. `app-layer/src/main/java/org/zzt/note/app/layer/content/req/NoteNodeContentQuery.java`

### 复用依赖（已存在）

1. `INoteNodeDomainService`
2. `IWordCardDomainService`
3. `IArticleDomainService`
4. `IQuestionDomainService`

## 5. 风险与注意事项

1. `NoteType` DTO 目前是字符串（`NoteNodeDTO.noteType`），resolver 内要做统一转换，避免大小写/编码差异。
2. 分页内容对象来自不同模块（`PageResultVO` 结构一致但字段细节可能不同），前端建议只依赖 `contentType + records/pageInfo` 共有字段。
3. `NoteNodeDomainService.get` 已含 `content`；app-layer 需要约定优先级，避免同名语义冲突（建议 app-layer 以 `contentType` 明确语义）。
4. 当前有些 domain controller 尚未完整覆盖业务场景，app-layer 要以“可查接口”先落地，不在首期承担写操作。

## 6. 最小可用版本（MVP）定义

1. 后端：聚合接口支持 `EMPTY/MARKDOWN/WORD_CARD/ARTICLE/QUESTIONS`。
2. 前端：单页面根据 `contentType` 切换渲染。
3. 兜底：未支持类型返回 `UNSUPPORTED`，页面展示“暂未支持该类型”。

---

如果你认可这个方向，下一步可以直接按第 1 阶段开始实现 app-layer 的骨架代码，我可以继续在当前分支直接落地。
