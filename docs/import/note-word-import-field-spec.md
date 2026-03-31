# Note + WordCard 导入 JSON 字段规格（给人/AI）

## 1. 文档目的

本文件定义导入接口 `POST /api/v1/imports/note-word` 的字段级协议，用于：

- 人工构造导入 JSON
- 让 AI 稳定解析、校验、生成导入数据
- 对齐后端当前导入行为（已移除 `relations`）

## 2. 顶层结构

```json
{
  "meta": { ... },
  "payload": {
    "noteNodes": [ ... ],
    "wordCards": [ ... ]
  }
}
```

## 3. 核心行为（非常重要）

1. `relations` 字段已废弃，不再接收。
2. 所有 `wordCards` 会在导入末尾自动关联到 `payload.noteNodes` 的最后一个元素（数组最后一项）。
3. 若 `wordCards` 非空且 `noteNodes` 为空，导入会报错。
4. 若最后一个 `noteNodes[*].nodeKey` 无法解析为实际节点，导入会报错。
5. 例句为必填：每个 `wordCards[*]` 必须至少包含 1 条例句（`sections.examples.items` 不得为空）。
6. `noteNode.noteType` 常用且受控为 `EMPTY`、`WORD_CARD` 两种；且只有最后一个节点允许使用 `WORD_CARD`。

## 4. 字段总览（扁平索引）

- `meta.version` string 可选
- `meta.source` string 可选
- `meta.importId` string 可选（建议唯一）
- `meta.createdAt` string 可选（ISO-8601）
- `meta.options.dryRun` boolean 可选（当前实现仅接收，不生效）
- `meta.options.upsert` boolean 可选（当前实现固定 REUSE_OR_CREATE）
- `meta.options.strict` boolean 可选（当前实现固定抛错中断）
- `payload.noteNodes` array 可选（默认 `[]`）
- `payload.wordCards` array 可选（默认 `[]`）

## 5. `meta` 详细说明

### 5.1 `meta`

- `version` string 可选
  - 协议版本号，建议如 `"1.0"`。
- `source` string 可选
  - 数据来源标识，建议如 `"manual-seed"`、`"ai-generated"`。
- `importId` string 可选
  - 导入批次号，建议全局唯一，便于追踪。
- `createdAt` string 可选
  - 导入数据生成时间，建议 ISO-8601（例如 `2026-03-31T10:00:00+08:00`）。
- `options` object 可选
  - 导入策略参数，当前服务仅接收字段，不改变核心逻辑。

### 5.2 `meta.options`

- `dryRun` boolean 可选，默认 `false`
  - 预演开关；当前实现不生效。
- `upsert` boolean 可选，默认 `true`
  - 覆盖/插入开关；当前实现不生效，始终按“存在复用，不存在创建”。
- `strict` boolean 可选，默认 `true`
  - 严格模式；当前实现始终按严格报错中断。

## 6. `payload.noteNodes[*]` 详细说明

### 6.1 结构

```json
{
  "nodeKey": "node-jp-word-card",
  "parentNodeKey": "node-root-jp",
  "noteNode": {
    "title": "日语单词卡",
    "noteType": "WORD_CARD",
    "sort": 10,
    "meta": {
      "icon": "🧠",
      "subject": "日语",
      "tags": [
        { "bizType": "NOTE_NODE", "label": "N5", "className": "tag-n5" }
      ]
    }
  },
  "content": {
    "desc": "该节点用于自动挂载单词卡"
  }
}
```

### 6.2 字段说明

- `nodeKey` string 必填
  - 导入内唯一业务键。
  - 落库对应：`note_node.note_key`。
  - 作用：判断复用/创建、父子关系解析、自动关联目标定位。

- `parentNodeKey` string 或 `null` 可选
  - 为 `null` 表示根节点。
  - 非空时可引用本次 `noteNodes` 内的 `nodeKey`，也可引用历史已存在节点键。

- `noteNode` object 可选（建议必填）
  - 节点基础信息。

- `noteNode.title` string 建议必填
  - 节点标题。

- `noteNode.noteType` string 建议必填
  - 节点类型，当前导入约束仅支持：`EMPTY`、`WORD_CARD`。
  - 规则：只有 `noteNodes` 最后一个元素允许使用 `WORD_CARD`；前面的节点应为 `EMPTY`。

- `noteNode.sort` number 可选
  - 排序值，默认按 `0` 处理。

- `noteNode.meta` object 可选
  - 节点元信息。

- `noteNode.meta.icon` string 可选
  - 节点图标。

- `noteNode.meta.subject` string 可选
  - 学科/主题。

- `noteNode.meta.tags` array 可选
  - 标签集合。

- `noteNode.meta.tags[*].bizType` string 建议必填
  - 标签业务类型。

- `noteNode.meta.tags[*].label` string 建议必填
  - 标签文本。

- `noteNode.meta.tags[*].className` string 可选
  - 标签样式类名。

- `content` any 可选
  - 支持 object/string/null。
  - 落库到 `note_node_content.content`（序列化字符串）。

### 6.3 当前后端行为

- `nodeKey` 已存在：复用历史节点，不覆盖更新原字段。
- `nodeKey` 不存在：创建新节点。
- 父链无法解析（父节点既不在本次也不在历史）：报错中断。

## 7. `payload.wordCards[*]` 详细说明

### 7.1 结构

```json
{
  "id": "jp-n5-watashi-001",
  "word": {
    "text": "私",
    "level": "N5"
  },
  "done": false,
  "tags": [
    { "name": "N5", "className": "tag-n5" },
    { "name": "代词", "className": "tag-pos" }
  ],
  "sections": {
    "meaning": {
      "collapsedByDefault": true,
      "meta": { "kana": "わたし", "zh": "我", "romaji": "watashi" },
      "description": "第一人称代词。"
    },
    "examples": {
      "collapsedByDefault": false,
      "items": [
        {
          "id": "ex-watashi-1",
          "sentence": "私は学生です。",
          "explain": {
            "collapsedByDefault": true,
            "reading": "わたしはがくせいです",
            "romaji": "watashi wa gakusei desu",
            "meaningZh": "我是学生。",
            "wordGrammarBreakdown": [
              { "word": "私", "kana": "わたし", "desc": "我" },
              { "word": "は", "kana": "", "desc": "助词。提示主题。" },
              { "word": "学生", "kana": "がくせい", "desc": "学生" },
              { "word": "です", "kana": "", "desc": "判断助动词。表示“是……”。" }
            ],
            "fixedPattern": {
              "pattern": "A は B です",
              "meaningZh": "A 是 B"
            }
          }
        }
      ]
    },
    "synonyms": {
      "items": [
        { "text": "わたくし", "kana": "わたくし" }
      ]
    },
    "related": {
      "items": [
        { "text": "僕", "kana": "ぼく" }
      ]
    }
  }
}
```

### 7.2 顶层字段说明

- `id` string 必填
  - 单词卡业务唯一键。
  - 落库对应：`word_card.card_code`。

- `word` object 可选（建议必填）
  - 单词信息。

- `word.text` string 建议必填
  - 单词文本。

- `word.level` string 可选
  - 难度级别，如 `N5`、`N4`。

- `done` boolean 可选，默认 `false`
  - 完成状态。

- `tags` array 可选
  - 单词卡标签。

- `tags[*].name` string 可选
  - 标签名。

- `tags[*].className` string 可选
  - 标签样式类名。

- `sections` object 可选（建议必填）
  - 分区内容。

- `actions` array 可选（不建议使用）
  - 历史字段，可被接收，但当前词卡导入场景建议不传，避免冗余。

### 7.3 `sections.meaning` 字段说明

- `sections.meaning.collapsedByDefault` boolean 可选，默认 `true`
- `sections.meaning.meta` object 可选
- `sections.meaning.meta.kana` string 可选
  - 可写单读音（如 `わたし`）或多读音并列（如 `よん／し`）。
- `sections.meaning.meta.zh` string 可选
- `sections.meaning.meta.romaji` string 可选
  - 当 `meta.kana` 为多读音时，建议并列罗马音（如 `yon / shi`）。
- `sections.meaning.description` string 可选
  - 建议写“词义 + 发音场景说明”。
  - 多读音词建议明确不同语境的读法（例如：数数常用读音、复合词常用读音、口语/正式语境差异）。

### 7.4 `sections.examples` 字段说明（强约束）

- `sections.examples.collapsedByDefault` boolean 可选，默认 `false`
- `sections.examples.items` array 必填且长度 >= 1
  - 每张卡片至少 1 条例句。

- `sections.examples.items[*].id` string 必填
  - 例句业务键，建议全局唯一。
  - 对于已存在卡片，后端按此字段去重同步例句。

- `sections.examples.items[*].sentence` string 必填
  - 原文例句。

- `sections.examples.items[*].explain` object 可选（建议必填）
- `sections.examples.items[*].explain.collapsedByDefault` boolean 可选，默认 `true`
- `sections.examples.items[*].explain.reading` string 可选
- `sections.examples.items[*].explain.romaji` string 可选
- `sections.examples.items[*].explain.meaningZh` string 建议必填
  - 表示该条例句的完整中文意思（整句翻译），不应仅填写目标词词义。

- `sections.examples.items[*].explain.wordGrammarBreakdown` array 可选
- 语义：逐词/逐短语拆解例句，核心用于说明“这个日文片段怎么读、中文是什么意思”。
- `sections.examples.items[*].explain.wordGrammarBreakdown[*].word` string 建议必填
  - 日文原词或短语（建议与 `sentence` 中片段一致）。
- `sections.examples.items[*].explain.wordGrammarBreakdown[*].kana` string 建议必填
  - 对应 `word` 的平假名读音。
  - 规则：助词/助动词可置空字符串 `""`；其余词建议填写读音。
- `sections.examples.items[*].explain.wordGrammarBreakdown[*].desc` string 建议必填
  - 中文释义（可包含词法说明），例如：`我`、`学生`、`判断助动词。表示“是……”。`。
  - 建议统一使用中文说明，不要直接回填日文原词。

- `sections.examples.items[*].explain.fixedPattern` object 可选
- `sections.examples.items[*].explain.fixedPattern.pattern` string 可选
- `sections.examples.items[*].explain.fixedPattern.meaningZh` string 可选

### 7.5 `sections.synonyms / related` 字段说明

- `sections.synonyms.items` array 可选
- `sections.related.items` array 可选
- `sections.synonyms.items[*].text` string 可选
- `sections.synonyms.items[*].kana` string 可选
- `sections.related.items[*].text` string 可选
- `sections.related.items[*].kana` string 可选
- 实践建议：`synonyms.items` 与 `related.items` 尽量不要长期为空，建议至少填充同类近义词或关联词（可先各 1 条起步）。

### 7.6 当前后端行为

- `id(card_code)` 已存在：复用历史卡片。
- `id(card_code)` 不存在：创建新卡片。
- 对已存在卡片：会按 `sections.examples.items[*].id` 增量补充缺失例句关系（已有不重复添加）。

## 8. 自动关联规则（替代 relations）

- 导入完成后，系统取 `payload.noteNodes` 的最后一个元素作为目标节点。
- `payload.wordCards` 中所有卡片都关联到该目标节点。
- 若某卡片与该节点关系已存在，计为 `reused`；否则创建并计为 `created`。

## 9. 校验规则

### 9.1 后端当前硬校验

- `noteNodes[*].nodeKey` 不能为空，且在 payload 内唯一。
- `noteNodes[*].noteNode.noteType` 仅允许 `EMPTY` 或 `WORD_CARD`（可为空，但建议填写）。
- 非最后节点不允许使用 `WORD_CARD`。
- `wordCards[*].id` 不能为空，且在 payload 内唯一。
- 当 `wordCards` 非空时，`noteNodes` 不可为空。
- 当 `wordCards` 非空时，最后一个 `noteNode.noteType` 必须为 `WORD_CARD`。
- 节点父链不可断裂。
- 每个 `wordCards[*].sections.examples.items` 必须至少 1 条。
- 每条例句必须提供 `id` 与 `sentence`。

### 9.2 协议强约束（调用方必须遵守）

- `wordCards[*].sections.examples.items[*].explain.meaningZh` 建议提供，便于后续学习展示与检索。
- `wordCards[*].word.text` 建议提供，避免卡片信息不完整。

### 9.3 团队约定（推荐执行）

- `meaningZh` 按“整句翻译”填写，不写成单词释义。
- `wordGrammarBreakdown[*].desc` 使用中文说明；助词、助动词建议写语法功能说明。
- 多读音词（如 `よん／し`）在 `sections.meaning.description` 写明常见使用场景。
- 词卡导入默认不传 `actions` 字段。
- `synonyms.items` 与 `related.items` 建议尽量非空。

## 10. 导入结果结构

返回 `NoteWordImportResult`：

```json
{
  "importId": "20260331-batch-001",
  "summary": {
    "noteNodes": { "total": 2, "created": 1, "reused": 1 },
    "wordCards": { "total": 1, "created": 0, "reused": 1 },
    "relations": { "total": 1, "created": 1, "reused": 0 }
  },
  "warnings": []
}
```

说明：`summary.relations.total` 现在等于 `wordCards` 数量（自动关联总数）。

## 11. 给 AI 的生成约束（建议直接复制）

1. 必须输出合法 JSON（UTF-8）。
2. `payload.noteNodes[*].nodeKey` 不得重复。
3. `payload.wordCards[*].id` 不得重复。
4. `payload.noteNodes` 非空时，最后一个节点必须是你希望挂卡片的目标节点。
5. 每个 `wordCards[*]` 必须包含至少 1 条例句（`sections.examples.items.length >= 1`）。
6. 每条例句必须包含 `id` 和 `sentence`。
7. 对用于挂卡片的节点，`noteNode.noteType` 建议设为 `WORD_CARD`。
8. 非最后节点不要使用 `WORD_CARD`，建议统一为 `EMPTY`。

## 12. 参考文件

- 结构方案：[note-word-import-structure.md](/Users/zhouzhitong/workroom/items/okx-core/app-note/docs/import/note-word-import-structure.md)
- 示例 JSON：[note-word-import-example.json](/Users/zhouzhitong/workroom/items/okx-core/app-note/docs/import/note-word-import-example.json)
- 联调请求：[importNoteWord.http](/Users/zhouzhitong/workroom/items/okx-core/app-note/http/importNoteWord.http)
