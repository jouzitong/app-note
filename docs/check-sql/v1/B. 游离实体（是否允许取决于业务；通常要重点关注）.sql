-- =====================================================
-- B. 游离实体（是否允许取决于业务；通常要重点关注）
-- =====================================================

-- B1. 游离单词：没有关联任何节点
SELECT w.*
FROM word_card w
         LEFT JOIN word_card_note_node_rel r ON r.word_card_id = w.id
WHERE r.word_card_id IS NULL;

-- B2. 游离例句：没有关联任何单词
SELECT e.*
FROM example_sentence e
         LEFT JOIN word_card_example_rel r ON r.example_sentence_id = e.id
WHERE r.example_sentence_id IS NULL;

-- B3. 游离节点内容：内容记录对应节点不存在
SELECT c.*
FROM note_node_content c
         LEFT JOIN note_node n ON n.id = c.node_id
WHERE n.id IS NULL;

-- B4. 游离节点meta：meta记录对应节点不存在
SELECT m.*
FROM note_node_meta m
         LEFT JOIN note_node n ON n.id = m.node_id
WHERE n.id IS NULL;
