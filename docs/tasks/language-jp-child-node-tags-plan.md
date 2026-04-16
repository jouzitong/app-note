# language-jp 子节点列表标签改造任务

- 文档日期：2026-04-16
- 适用范围：`data-core` + `note-ui`
- 目标页面：`/language-jp/materials/:id`
- 参考原型：`static-product/module/language_jp/materials/index.html`

## 1. 背景

原型页中，子节点列表有标签展示（`tree-tags/tree-tag`）。  
当前实现中，子节点列表只展示标题和操作按钮，没有标签。

已确认现状：

1. 前端子节点映射未保留标签字段（只取 `id/title/noteType`）。
2. `GET /api/v1/noteNodes/domain/{id}` 返回的 `childNoteNodes` 结构不包含标签。
3. 后端 `NoteNodeMeta.tags` 实体关系已存在，编辑页也可写入 `meta.tags`。

## 2. 目标

1. 子节点列表可展示标签，视觉对齐原型。
2. 接口稳定返回子节点标签数据，不影响旧调用方。
3. 保持当前子节点跳转、编辑、删除行为不变。

## 3. 改造方案（基于现状）

### 3.1 后端接口层

1. 新增子节点详情 VO（建议：`NoteNodeChildVO`）：
   1. `id`
   2. `title`
   3. `noteType`
   4. `sort`
   5. `tags: List<NoteTagDTO>`
2. 在 `NoteNodeVO` 增加新字段（建议：`childNoteNodeDetails`）。
3. 保留旧字段 `childNoteNodes`，兼容历史前端。

### 3.2 后端服务层

1. 在 `NoteNodeDomainServiceImpl#get` 中，子节点查询改为可带出 `meta.tags`。
2. 将子节点映射到新 VO（含 `tags`）。
3. 规避 N+1（`@EntityGraph` 或 fetch join）。

### 3.3 前端页面

文件：`note-ui/src/views/commons/notes/index.vue`

1. `childNodes` 映射增加 `tags`。
2. 模板补充 `tree-tags/tree-tag` 渲染区。
3. 增加样式（标签换行、间距、默认样式）。
4. 接口兼容：
   1. 优先读 `childNoteNodeDetails`
   2. 回退 `childNoteNodes`（无标签时空数组）

## 4. 缺失内容清单（逐条核对）

### 4.1 接口与模型缺失

- [ ] 缺失子节点“含标签”VO（当前只有 `NoteNodePathVO`）。
- [ ] 缺失 `NoteNodeVO` 的子节点标签字段（当前 `childNoteNodes` 不含 tags）。
- [ ] 缺失 `get` 接口子节点标签组装逻辑。

### 4.2 查询与性能缺失

- [ ] 缺失带 `meta.tags` 的子节点查询方法（当前 `findByParentIdOrderBySortAsc` 为基础查询）。
- [ ] 缺失 N+1 风险规避方案（EntityGraph/fetch join 未落地）。

### 4.3 前端渲染缺失

- [ ] 缺失子节点标签数据映射（`index.vue` 当前未映射 `tags`）。
- [ ] 缺失子节点标签 DOM（`tree-tags/tree-tag`）。
- [ ] 缺失子节点标签样式（当前仅有标题与操作按钮样式）。

### 4.4 文档与验证缺失

- [ ] 缺失接口返回字段说明（新增字段语义、兼容策略）。
- [ ] 缺失联调用例（有标签/无标签/空 meta 三类数据）。
- [ ] 缺失回归检查项（子节点跳转、编辑、删除不受影响）。

## 5. 验收标准

1. 子节点有标签数据时可见标签；无标签数据时正常展示标题，不报错。
2. 旧字段调用方不受影响（兼容旧 `childNoteNodes`）。
3. 列表交互不回归：
   1. 点击子节点仍可进入详情/词卡
   2. 编辑删除按钮事件隔离正常
4. 无新增 lint/build 错误。

## 6. 实施顺序建议

1. 先改后端返回结构和查询，再前端渲染。
2. 前端先做兼容读取（新字段优先、旧字段兜底）。
3. 最后补接口文档和回归清单。
