-- =====================================================
-- A. 关系表引用断裂（最常见脏数据）
-- =====================================================

-- A1. 节点-单词关系：单词不存在
SELECT r.*
FROM word_card_note_node_rel r
         LEFT JOIN word_card w ON w.id = r.word_card_id
WHERE w.id IS NULL;

-- A2. 节点-单词关系：节点不存在
SELECT r.*
FROM word_card_note_node_rel r
         LEFT JOIN note_node n ON n.id = r.note_node_id
WHERE n.id IS NULL;

-- A3. 单词-例句关系：单词不存在
SELECT r.*
FROM word_card_example_rel r
         LEFT JOIN word_card w ON w.id = r.word_card_id
WHERE w.id IS NULL;

-- A4. 单词-例句关系：例句不存在
SELECT r.*
FROM word_card_example_rel r
         LEFT JOIN example_sentence e ON e.id = r.example_sentence_id
WHERE e.id IS NULL;

-- A5. 节点meta-标签关系：meta不存在
SELECT r.*
FROM note_node_meta_tag_rel r
         LEFT JOIN note_node_meta m ON m.id = r.meta_id
WHERE m.id IS NULL;

-- A6. 节点meta-标签关系：tag不存在
SELECT r.*
FROM note_node_meta_tag_rel r
         LEFT JOIN note_tag t ON t.id = r.tag_id
WHERE t.id IS NULL;
