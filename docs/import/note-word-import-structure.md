# 笔记节点 + 单词卡 批量导入 JSON 结构（落地方案）

## 1. 目标

用一个 JSON 文件同时导入：

- `noteNodes`（笔记树节点）
- `wordCards`（单词卡）
- `relations`（节点与卡片关系）

并最终写入如下后端模型：

- `note_node` / `note_node_content` / `note_node_meta`
- `word_card` / `example_sentence` / `note_tag`
- `word_card_note_node_rel`

## 2. 顶层结构（建议）

```json
{
  "meta": {
    "version": "1.0",
    "source": "import-tool",
    "importId": "20260331-batch-001",
    "createdAt": "2026-03-31T10:00:00+08:00",
    "options": {
      "dryRun": false,
      "upsert": true,
      "strict": true
    }
  },
  "payload": {
    "noteNodes": [],
    "wordCards": [],
    "relations": []
  }
}
```

## 3. 字段设计

### 3.1 payload.noteNodes

每个元素建议结构：

```json
{
  "nodeKey": "node-jp-pronoun",
  "parentNodeKey": null,
  "noteNode": {
    "title": "日语代词",
    "noteType": "MARKDOWN",
    "sort": 10,
    "meta": {
      "icon": "📘",
      "subject": "日语",
      "tags": [
        { "bizType": "NOTE_NODE", "label": "N5", "className": "tag-n5" },
        { "bizType": "NOTE_NODE", "label": "语法", "className": "tag-grammar" }
      ]
    }
  },
  "content": {
    "paragraphs": ["代词总览"],
    "bullets": ["私", "あなた", "彼"]
  }
}
```

约束建议：

- `nodeKey`：导入文件内唯一，用于建立父子关系和 card 关联。
- `parentNodeKey`：为空表示根节点；非空必须引用已存在或同文件中的 `nodeKey`。
- `noteNode.noteType`：建议用枚举名（`MARKDOWN` / `WORD_CARD` / `SENTENCE` / `QUESTIONS`）。
- `content`：对象或字符串均可，最终落到 `note_node_content.content`。

### 3.2 payload.wordCards

每个元素建议结构：

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
              { "word": "私", "desc": "我" },
              { "word": "は", "desc": "主题助词" }
            ],
            "fixedPattern": { "pattern": "A は B です", "meaningZh": "A 是 B" }
          }
        }
      ]
    },
    "synonyms": { "items": [{ "text": "僕", "kana": "ぼく" }] },
    "related": { "items": [{ "text": "あなた", "kana": "" }] }
  },
  "actions": [
    { "key": "done", "icon": "✓", "title": "完成" },
    { "key": "audio", "icon": "🔊", "title": "发音" }
  ]
}
```

约束建议：

- `id`：业务唯一编码，对应后端 `word_card.card_code`，同时作为 `relations.cardId` 的引用键。
- `sections.examples.items[*].id`：建议提供稳定业务 ID，避免重复导入产生新例句。

### 3.3 payload.relations

每个元素建议结构：

```json
{
  "nodeKey": "node-jp-pronoun",
  "cardId": "jp-n5-watashi-001",
  "order": 1
}
```

约束建议：

- `nodeKey` 必须在 `noteNodes` 中可解析；`cardId` 必须在 `wordCards[*].id` 中可解析。
- 同一 `nodeKey` 可关联多个 `cardId`（由 `order` 控制展示顺序）。
- 可增加唯一约束：`(nodeKey, cardId)` 不重复。

## 4. 导入顺序（强约束）

建议按 5 步执行：

1. 预校验
- 校验 `nodeKey` 唯一性，校验 `wordCards[*].id` 唯一性。
- 校验 `parentNodeKey`、`relations` 引用完整性。
- 校验 `noteType`、关键必填字段合法性。

2. 导入 noteNodes
- 按拓扑顺序（父先子后）创建或更新 `note_node`。
- 记录 `nodeKey -> noteNodeId` 映射。
- 同步 `note_node_content`。

3. 导入 wordCards
- 按 `id(card_code)` 做 upsert。
- 处理 `tags/examples` 的复用与新增。
- 记录 `cardId(id) -> wordCardId` 映射。

4. 导入 relations
- 根据映射写 `word_card_note_node_rel(note_node_id, word_card_id)`。
- 幂等去重，避免重复关系。

5. 收尾
- 返回导入报告（成功/失败/跳过计数 + 明细）。

## 5. 幂等与冲突策略（建议）

- `upsert=true`：
  - `noteNodes` 以 `nodeKey` + 可选外部映射表定位（或按标题+parent做弱匹配，不推荐）。
  - `wordCards` 以 `id(card_code)` 作为强唯一。
- `strict=true`：
  - 任一关键错误直接中断（事务回滚）。
- `strict=false`：
  - 记录错误并跳过错误记录，继续后续数据。

## 6. 错误报告结构（建议）

```json
{
  "importId": "20260331-batch-001",
  "summary": {
    "noteNodes": { "total": 100, "success": 98, "failed": 2 },
    "wordCards": { "total": 800, "success": 790, "failed": 10 },
    "relations": { "total": 800, "success": 788, "failed": 12 }
  },
  "errors": [
    {
      "scope": "wordCards",
      "key": "card-watashi",
      "code": "DUPLICATE_CARD_CODE",
      "message": "id(card_code) already exists with conflicting data"
    }
  ]
}
```

## 7. 与当前系统的关键对齐点

- `noteType=WORD_CARD` 的节点并不会自动绑定卡片，必须依赖 `relations`（`cardId=id`）写入 `word_card_note_node_rel`。
- `WordCardDomainService.get(noteId,index)` 强依赖关系表，因此批量导入必须包含关系处理。
- `content` 允许对象，后端会序列化到 `note_node_content.content`。
