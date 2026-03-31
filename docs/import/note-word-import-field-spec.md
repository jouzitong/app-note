# Note + WordCard 导入 JSON 字段规格（给人/AI）

## 1. 文档目的

本文件定义导入接口 `POST /api/v1/imports/note-word` 的**字段级协议**，用于：

- 人工构造导入 JSON
- 让 AI 稳定解析/校验/生成导入数据
- 对齐后端落库行为（当前实现）

## 2. 顶层结构

```json
{
  "meta": { ... },
  "payload": {
    "noteNodes": [ ... ],
    "wordCards": [ ... ],
    "relations": [ ... ]
  }
}
```

## 3. 字段总览（扁平索引）

- `meta.version` string 可选
- `meta.source` string 可选
- `meta.importId` string 可选（建议唯一）
- `meta.createdAt` string 可选（ISO-8601）
- `meta.options.dryRun` boolean 可选（当前实现仅接收，不生效）
- `meta.options.upsert` boolean 可选（当前实现固定 REUSE_OR_CREATE）
- `meta.options.strict` boolean 可选（当前实现固定抛错中断）
- `payload.noteNodes` array 可选（默认 `[]`）
- `payload.wordCards` array 可选（默认 `[]`）
- `payload.relations` array 可选（默认 `[]`）

---

## 4. `payload.noteNodes[*]` 规格

### 4.1 结构

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
    "desc": "该节点用于挂载单词卡关系"
  }
}
```

### 4.2 字段说明

- `nodeKey` string 必填
  - 导入内唯一键。
  - 落库：`note_node.note_key`。
  - 用途：判断“复用历史/新建节点”、`relations.nodeKey` 引用。

- `parentNodeKey` string/null 可选
  - `null` 表示根节点。
  - 非空时表示父节点业务键，可引用：
    - 本次 `noteNodes` 内其他 `nodeKey`
    - 历史已存在的 `note_node.note_key`

- `noteNode` object 必填
  - 节点基础信息。

- `noteNode.title` string 建议必填
  - 标题。

- `noteNode.noteType` string 建议必填
  - 建议值：`MARKDOWN` / `WORD_CARD` / `SENTENCE` / `QUESTIONS`。

- `noteNode.sort` number 可选
  - 默认 `0`。

- `noteNode.meta` object 可选
  - 元信息。

- `noteNode.meta.icon` string 可选
- `noteNode.meta.subject` string 可选

- `noteNode.meta.tags` array 可选
  - 标签列表，元素结构：
    - `bizType` string 必填
    - `label` string 必填
    - `className` string 可选
  - 落库复用规则：按 `(bizType, label)` 先查后建，避免重复。

- `content` any 可选
  - 支持 object/string/null。
  - 落库到 `note_node_content.content`（序列化字符串）。

### 4.3 当前后端行为（已实现）

- 若 `nodeKey` 已存在：**复用历史节点，不更新字段**。
- 若 `nodeKey` 不存在：创建新节点。
- 创建时按父键解析 `parentId`；无法解析会报错。

---

## 5. `payload.wordCards[*]` 规格

### 5.1 结构

```json
{
  "id": "jp-n5-watashi-001",
  "word": { "text": "私", "level": "N5" },
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
              { "word": "私", "desc": "我" }
            ],
            "fixedPattern": { "pattern": "A は B です", "meaningZh": "A 是 B" }
          }
        }
      ]
    },
    "synonyms": { "items": [] },
    "related": { "items": [] }
  },
  "actions": [
    { "key": "done", "icon": "✓", "title": "完成" }
  ]
}
```

### 5.2 关键字段

- `id` string 必填
  - 单词卡业务唯一键。
  - 落库：`word_card.card_code`。
  - 用途：复用/新建判断，`relations.cardId` 引用。

- 其余字段遵循 `WordCardVO` 结构（与 `server-word` 现有领域接口一致）。

### 5.3 当前后端行为（已实现）

- 若 `id(card_code)` 已存在：**复用历史卡片，不更新字段**。
- 若不存在：调用 `wordCardDomainService.add` 新建。

---

## 6. `payload.relations[*]` 规格

### 6.1 结构

```json
{
  "nodeKey": "node-jp-word-card",
  "cardId": "jp-n5-watashi-001",
  "order": 1
}
```

### 6.2 字段说明

- `nodeKey` string 必填
  - 必须能解析到一个实际 `note_node.id`。

- `cardId` string 必填
  - 必须能解析到一个实际 `word_card.id`（通过 `card_code` 查到）。

- `order` number 可选
  - 当前仅用于导入时排序，不落库。

### 6.3 当前后端行为（已实现）

- 若关系 `(wordCardId, noteNodeId)` 已存在：计为 `reused`。
- 否则插入 `word_card_note_node_rel`，计为 `created`。

---

## 7. 校验规则（当前实现）

- `noteNodes[*].nodeKey` 不能为空，且在当前 payload 内唯一。
- `wordCards[*].id` 不能为空，且在当前 payload 内唯一。
- `relations[*].nodeKey` 与 `relations[*].cardId` 不能为空。
- 父链无法解析时会报错中断。
- 关系目标无法解析（找不到节点或卡片）时会报错中断。

---

## 8. 导入结果结构

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

---

## 9. 给 AI 的生成约束（建议直接复制）

1. 必须输出合法 JSON（UTF-8）。
2. `payload.noteNodes[*].nodeKey` 不得重复。
3. `payload.wordCards[*].id` 不得重复。
4. `payload.relations[*].nodeKey` 必须引用存在的 `nodeKey`。
5. `payload.relations[*].cardId` 必须引用存在的 `wordCards[*].id`。
6. 对需要挂单词卡的节点，`noteType` 设为 `WORD_CARD`。
7. 若不确定字段值，允许填空字符串，但不可省略 `nodeKey`/`id`/`relations` 引用字段。

---

## 10. 参考文件

- 结构方案：[note-word-import-structure.md](/Users/zhouzhitong/workroom/items/okx-core/app-note/docs/import/note-word-import-structure.md)
- 示例 JSON：[note-word-import-example.json](/Users/zhouzhitong/workroom/items/okx-core/app-note/docs/import/note-word-import-example.json)
- 联调请求：[importNoteWord.http](/Users/zhouzhitong/workroom/items/okx-core/app-note/http/importNoteWord.http)
