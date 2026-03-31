-- =====================================================
-- F. 学习进度引用异常
-- =====================================================

-- F1. user_word_progress 指向不存在的单词
SELECT u.*
FROM user_word_progress u
         LEFT JOIN word_card w ON w.id = u.word_card_id
WHERE w.id IS NULL;

-- F2. 同一用户+单词重复进度（理论上有唯一约束）
SELECT user_id, word_card_id, COUNT(*) cnt
FROM user_word_progress
GROUP BY user_id, word_card_id
HAVING COUNT(*) > 1;
