# ArticleReader 多文章聚合与接口收敛改造执行清单

## 1. 改造背景与目标

当前 `ArticleReader` 页面存在以下问题：
1. 前端依赖多条写接口（收藏/语速/段落位置）来维持阅读状态，链路分散。
2. 页面交互仍是“段落导航”，但业务已变为“一个 `noteNodeId` 关联多篇文章”。
3. “核心知识点”在前端推断生成，后端无数据源，导致语义不可控、无法运营维护。

本次改造目标：
1. 以 `GET /api/v1/articles/domain/note-node/{noteNodeId}` 作为阅读页唯一接口（读聚合）。
2. 页面导航从“上一段/下一段”切换为“上一篇/下一篇文章”。
3. 核心知识点改为后端 JSON 元数据维护（落在 `ArticleMetaInfo` 中）。
4. 返回数据中直接包含 `favorite`、`completed`，供前端展示与状态恢复。

## 2. 业务重构后模型（To-Be）

### 2.1 阅读页业务对象

以“阅读会话”建模，按 `noteNodeId` 一次返回多篇文章：
1. `noteNodeId`：当前节点。
2. `currentArticleIndex`：默认进入的文章索引（可由服务端策略决定，如第一篇未完成文章）。
3. `articles[]`：该节点下文章列表（有序）。

### 2.2 单篇文章视图对象（建议）

每篇文章包含：
1. 基础信息：`id`、`title`。
2. 正文数据：`paragraphs`、`translation`。
3. 状态信息：`progress.favorite`、`progress.completed`。
4. 知识点：`knowledge.coreVocabulary[]`、`knowledge.coreSentencePatterns[]`。

### 2.3 交互语义变化

1. “上一篇/下一篇”表示文章间切换，不再表示段落切换。
2. 点击段落不再触发保存位置。
3. 语速只影响本次播放行为（如需长期保存，另起需求；本次不做写接口）。

## 3. 接口契约改造清单

## 3.1 目标接口

保留并重构：
1. `GET /api/v1/articles/domain/note-node/{noteNodeId}`

返回从“单篇 `ArticleVO`”升级为“阅读页聚合 VO”（命名建议：`ArticleReaderVO`）。

### 3.2 建议响应结构（草案）

```json
{
  "noteNodeId": 123,
  "currentArticleIndex": 0,
  "articles": [
    {
      "id": "article_001",
      "title": "私の毎日の生活",
      "paragraphs": [[{"text": "私", "kana": "わたし"}]],
      "translation": ["我每天早上七点起床。"],
      "progress": {
        "favorite": true,
        "completed": false
      },
      "knowledge": {
        "coreVocabulary": [
          {"jp": "毎朝", "kana": "まいあさ", "meaning": "每天早晨"}
        ],
        "coreSentencePatterns": [
          {"jp": "〜は 〜に 〜ます", "meaning": "在某时间做某事"}
        ]
      }
    }
  ]
}
```

### 3.3 兼容策略

1. 旧前端依赖的 `POST /favorite`、`POST /playback-rate`、`POST /position` 暂不立即删除。
2. 先标记为“阅读页不再使用”，待全链路回归后再进行接口下线评估。

## 4. 后端执行清单

### 4.1 VO 与 DTO 层

1. 新增阅读页聚合 VO（建议：`ArticleReaderVO`）。
2. 在文章项中保留现有 `ArticleVO` 的内容字段，补充或裁剪为阅读页最小必要字段。
3. `progress` 中增加 `completed` 字段（与 `favorite` 并列）。
4. 增加 `knowledge` 结构定义：
   1. `coreVocabulary[]`（`jp`、`kana`、`meaning`）
   2. `coreSentencePatterns[]`（`jp`、`meaning`）

### 4.2 元信息 JSON（ArticleMetaInfo）

1. 在 `ArticleMetaInfo` 新增 `knowledge` 字段。
2. 新增嵌套对象定义并确保 Jackson 序列化兼容。
3. `toVO` 转换逻辑改为：优先读取 `metaInfo.knowledge`，为空时返回空数组，不做前端推断。

### 4.3 服务层（IArticleDomainService / Impl）

1. `getByNoteNodeId(noteNodeId)` 改为返回聚合 VO。
2. 按 `article_note_node_rel` 全量查该节点文章（`order by article_id asc`）。
3. 批量拉取文章与进度，组装 `articles[]`。
4. `currentArticleIndex` 规则建议：
   1. 优先第一篇 `completed=false`。
   2. 若全部完成，返回 `0`。
5. 补充参数校验与空列表语义（返回空 `articles` 或抛错，需统一约定）。

### 4.4 进度数据（favorite/completed）

1. `ArticleUserProgress` 增加 `completed` 字段（布尔，默认 `false`）。
2. `ArticleVO.Progress` 与组装逻辑同步补齐 `completed`。
3. 若历史数据不存在该字段，读取时按默认 `false` 回填。

### 4.5 Controller 层

1. `ArticleDomainController#getByNoteNode` 返回类型改为聚合 VO。
2. 统一接口注释，声明该接口用于阅读页全量数据获取。

## 5. 数据库与迁移清单

### 5.1 表结构变更

1. `article_user_progress` 增加列：`completed` `tinyint(1)` `not null default 0`。
2. 更新初始化 SQL 与增量 SQL：
   1. `docs/sql/init/note.sql`
   2. `docs/sql/v2/article-reader.sql`（或新增 v3 migration）

### 5.2 历史数据兼容

1. 为已有数据批量回填 `completed=0`。
2. 验证旧记录读取不抛异常。

## 6. 前端执行清单

### 6.1 API 与 Service

1. `note-ui/src/api/articles.js`
   1. `getArticleByNoteNode` 响应类型切换为“聚合对象”。
2. `note-ui/src/views/domain/language-jp/services/article.service.js`
   1. 新增/改造 mapper：聚合 DTO -> 阅读页 VM。
   2. 删除阅读页对 `saveArticleFavorite/saveArticlePlaybackRate/saveArticlePosition` 的调用依赖。

### 6.2 Mapper

1. `article.mapper` 新增阅读页级别映射：
   1. `mapArticleReaderDtoToVm`
   2. 对 `knowledge` 字段做空值归一化（默认空数组）。
2. 删除“核心知识点前端推断”依赖所需的旧辅助逻辑。

### 6.3 ArticleReader.vue

1. 状态重构：
   1. 增加 `articles`、`currentArticleIndex`。
   2. `article` 改为计算属性（从 `articles[currentArticleIndex]` 派生）。
2. 交互重构：
   1. 按钮文案/语义改为“上一篇/下一篇（文章）”。
   2. `goPrev/goNext` 切换文章索引。
   3. 移除 `selectParagraph -> persistPosition` 写接口调用链。
3. 核心知识点渲染：
   1. 改为读取后端 `knowledge`。
   2. 移除前端正则推断模块。
4. 收藏/完成展示：
   1. 收藏图标展示后端返回状态。
   2. 完成状态增加展示标签（是否可交互由产品另定；本次默认只读）。

### 6.4 兼容与回退

1. 若响应中无 `knowledge`，前端按空列表显示，不报错。
2. 若 `articles` 为空，展示“暂无文章内容”。

## 7. 验收标准（DoD）

1. 阅读页仅依赖一个接口即可完整渲染。
2. 同一 `noteNodeId` 下可在多篇文章之间切换。
3. 页面不再触发 `POST /playback-rate`、`POST /position`、`POST /favorite`。
4. `favorite`、`completed` 可从 GET 返回并正确展示。
5. 核心知识点完全来自后端 `ArticleMetaInfo`，前端无推断逻辑残留。
6. 历史数据（无 `completed`、无 `knowledge`）可平稳显示。

## 8. 测试清单

### 8.1 后端单测/集成测试

1. `noteNodeId` 正常返回多文章顺序。
2. 空节点返回语义测试（空列表或异常，按约定断言）。
3. `knowledge` JSON 反序列化正确。
4. `favorite/completed` 默认值与回填行为正确。

### 8.2 前端测试

1. 页面初始化渲染（有文章/无文章）。
2. 上一篇/下一篇边界行为。
3. 核心知识点渲染与空数据兜底。
4. 确认不再调用旧写接口（可通过 mock 断言）。

### 8.3 联调回归

1. 后端新响应结构与前端 mapper 对齐。
2. 多文章切换时播放功能不受影响。
3. 旧页面入口与路由参数不变。

## 9. 实施顺序建议

1. 后端先行：VO/MetaInfo/Service/Controller/SQL 完成并自测通过。
2. 前端再改：API/mapper/组件交互一次性替换。
3. 联调与回归：重点验证“多文章 + 核心知识点 + 兼容空数据”。
4. 稳定后再评估下线旧写接口。

## 10. 风险与决策项

### 10.1 风险

1. 接口结构从单篇变聚合，前端映射层改动面较大。
2. `completed` 新增字段涉及 DB 变更，需要保证生产兼容。
3. 历史文章 `metaInfo` 可能无 `knowledge`，必须做好空值兜底。

### 10.2 待最终确认（建议在开发前冻结）

1. `completed` 是否只读展示，还是后续需要可写操作。
2. `currentArticleIndex` 策略是否固定为“首篇未完成优先”。
3. 空节点返回语义采用 `200 + []` 还是业务异常。

## 11. 任务分配模板（可直接贴到迭代）

1. 后端任务 A：聚合查询接口与 VO 改造。
2. 后端任务 B：`ArticleMetaInfo` 增加 `knowledge` + 数据转换。
3. 后端任务 C：`completed` 字段落库与迁移脚本。
4. 前端任务 D：`ArticleReader` 状态模型与导航交互重构。
5. 前端任务 E：知识点改为后端渲染并移除推断逻辑。
6. 联调任务 F：接口契约对齐与回归用例补齐。
