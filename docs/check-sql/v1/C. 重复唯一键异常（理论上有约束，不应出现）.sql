-- =====================================================
-- C. 重复/唯一键异常（理论上有约束，不应出现）
-- =====================================================

-- C1. note_key 重复（忽略 NULL）
SELECT note_key, COUNT(*) cnt
FROM note_node
WHERE note_key IS NOT NULL AND note_key <> ''
GROUP BY note_key
HAVING COUNT(*) > 1;

-- C2. 单词业务键 card_code 重复
SELECT card_code, COUNT(*) cnt
FROM word_card
GROUP BY card_code
HAVING COUNT(*) > 1;

-- C3. 例句业务键 example_code 重复
SELECT example_code, COUNT(*) cnt
FROM example_sentence
GROUP BY example_code
HAVING COUNT(*) > 1;

-- C4. 节点-单词关系重复
SELECT word_card_id, note_node_id, COUNT(*) cnt
FROM word_card_note_node_rel
GROUP BY word_card_id, note_node_id
HAVING COUNT(*) > 1;

-- C5. 单词-例句关系重复
SELECT word_card_id, example_sentence_id, COUNT(*) cnt
FROM word_card_example_rel
GROUP BY word_card_id, example_sentence_id
HAVING COUNT(*) > 1;
