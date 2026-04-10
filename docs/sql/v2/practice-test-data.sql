-- 做题/练习模块测试数据（MySQL 8+）
--
-- 用法：
-- 1) 先执行建表：docs/sql/v2/practice.sql
-- 2) 找到一个 NoteType=QUESTIONS 的 note_node.id，填到下面的 @note_node_id
-- 3) 执行本脚本，即可在该节点下生成 3 道题（选择/听力/填空）
--
-- 注意：
-- - 本脚本不会创建 note_node（避免与你本地 note_node 表结构不一致）。
-- - 若你希望从前端直接跑通：路由 /language-jp/note/{parentId}/practice?nodeId=@note_node_id

SET @note_node_id = 1;  -- TODO：改成你的 QUESTIONS 节点ID

-- ========== Q1：选择题 ==========
INSERT INTO `question` (
  `question_code`, `title`, `stem`, `question_type`, `difficulty`, `locale`, `meta_info`,
  `version`, `create_time`, `created_by`, `update_time`, `updated_by`
)
VALUES (
  'n3_mock_001',
  '次の文の（　）に入る言葉として、最もよいものを一つ選びなさい。',
  '昨日は疲れていたので、家に帰ってすぐ（　）。',
  'SINGLE_CHOICE',
  2,
  'ja-JP',
  JSON_OBJECT(
    'display', JSON_OBJECT('typeLabel', '选择题', 'section', '文字・語彙', 'score', 1),
    'options', JSON_ARRAY(
      JSON_OBJECT('key', '1', 'content', JSON_OBJECT('type', 'text', 'text', '寝ていました'), 'sort', 1),
      JSON_OBJECT('key', '2', 'content', JSON_OBJECT('type', 'text', 'text', '寝ました'), 'sort', 2),
      JSON_OBJECT('key', '3', 'content', JSON_OBJECT('type', 'text', 'text', '寝られます'), 'sort', 3),
      JSON_OBJECT('key', '4', 'content', JSON_OBJECT('type', 'text', 'text', '寝させました'), 'sort', 4)
    ),
    'answerKey', JSON_OBJECT('optionKeys', JSON_ARRAY('2')),
    'analysis', JSON_OBJECT('type', 'text', 'text', '句子表示过去发生的动作，且“回家后马上睡了”，用过去式「寝ました」最自然。'),
    'assets', JSON_ARRAY(),
    'ext', JSON_OBJECT('source', 'mock')
  ),
  1, NOW(), 1, NOW(), 1
);
SET @q1_id = LAST_INSERT_ID();

INSERT INTO `question_note_node_rel` (`question_id`, `note_node_id`)
VALUES (@q1_id, @note_node_id);

-- ========== Q2：听力选择题 ==========
INSERT INTO `question` (
  `question_code`, `title`, `stem`, `question_type`, `difficulty`, `locale`, `meta_info`,
  `version`, `create_time`, `created_by`, `update_time`, `updated_by`
)
VALUES (
  'n3_mock_002',
  '問題を聞いて、正しい答えを一つ選んでください。',
  '先听音频，再选择正确答案。',
  'SINGLE_CHOICE',
  2,
  'ja-JP',
  JSON_OBJECT(
    'display', JSON_OBJECT('typeLabel', '听力选择题', 'section', '聴解', 'score', 1),
    'assets', JSON_ARRAY(
      JSON_OBJECT('type', 'audio', 'url', 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3', 'meta', JSON_OBJECT('format', 'mp3'))
    ),
    'options', JSON_ARRAY(
      JSON_OBJECT('key', '1', 'content', JSON_OBJECT('type', 'text', 'text', '男の人は図書館へ行きます'), 'sort', 1),
      JSON_OBJECT('key', '2', 'content', JSON_OBJECT('type', 'text', 'text', '女の人は駅で待ちます'), 'sort', 2),
      JSON_OBJECT('key', '3', 'content', JSON_OBJECT('type', 'text', 'text', '二人は午後三時に会います'), 'sort', 3),
      JSON_OBJECT('key', '4', 'content', JSON_OBJECT('type', 'text', 'text', '男の人は今日は行きません'), 'sort', 4)
    ),
    'answerKey', JSON_OBJECT('optionKeys', JSON_ARRAY('3')),
    'analysis', JSON_OBJECT('type', 'text', 'text', '这是听力题示例：先用占位音频。你可以替换成自己的日语 mp3。'),
    'ext', JSON_OBJECT('source', 'mock')
  ),
  1, NOW(), 1, NOW(), 1
);
SET @q2_id = LAST_INSERT_ID();

INSERT INTO `question_note_node_rel` (`question_id`, `note_node_id`)
VALUES (@q2_id, @note_node_id);

-- ========== Q3：填空题 ==========
INSERT INTO `question` (
  `question_code`, `title`, `stem`, `question_type`, `difficulty`, `locale`, `meta_info`,
  `version`, `create_time`, `created_by`, `update_time`, `updated_by`
)
VALUES (
  'n3_mock_003',
  '文脈に合う言葉をひらがな、または漢字で書きなさい。',
  'このかばんは少し重いですが、まだ（　）です。',
  'FILL_BLANK',
  2,
  'ja-JP',
  JSON_OBJECT(
    'display', JSON_OBJECT('typeLabel', '单词填写题', 'section', '語彙', 'score', 1),
    'blanks', JSON_ARRAY(
      JSON_OBJECT('key', 'blank_1', 'hint', 'ひらがな、または漢字')
    ),
    'answerKey', JSON_OBJECT(
      'blanks', JSON_OBJECT('blank_1', JSON_ARRAY('だいじょうぶ', '大丈夫'))
    ),
    'analysis', JSON_OBJECT('type', 'text', 'text', '句意是“这个包虽然有点重，但还没问题”，所以填「だいじょうぶ / 大丈夫」。'),
    'ext', JSON_OBJECT('source', 'mock')
  ),
  1, NOW(), 1, NOW(), 1
);
SET @q3_id = LAST_INSERT_ID();

INSERT INTO `question_note_node_rel` (`question_id`, `note_node_id`)
VALUES (@q3_id, @note_node_id);

-- ========== 可选：插入一条错题进度与作答记录（用于测试 stats / wrong-questions） ==========
-- 说明：后端默认 resolveUserId 为 1，因此这里也用 user_id=1

INSERT INTO `user_question_progress` (
  `user_id`, `question_id`, `status`,
  `wrong_count`, `correct_count`, `correct_streak`,
  `last_answered_at`, `last_wrong_at`, `last_correct_at`,
  `version`, `create_time`, `created_by`, `update_time`, `updated_by`
)
VALUES (
  1, @q2_id, 'WRONG',
  1, 0, 0,
  NOW(), NOW(), NULL,
  1, NOW(), 1, NOW(), 1
)
ON DUPLICATE KEY UPDATE
  `status` = VALUES(`status`),
  `wrong_count` = VALUES(`wrong_count`),
  `correct_count` = VALUES(`correct_count`),
  `correct_streak` = VALUES(`correct_streak`),
  `last_answered_at` = VALUES(`last_answered_at`),
  `last_wrong_at` = VALUES(`last_wrong_at`),
  `last_correct_at` = VALUES(`last_correct_at`),
  `update_time` = VALUES(`update_time`),
  `updated_by` = VALUES(`updated_by`);

INSERT INTO `practice_answer_record` (
  `user_id`, `question_id`, `note_node_id`, `session_id`, `client_request_id`,
  `user_answer`, `result`, `cost_ms`, `answered_at`,
  `version`, `create_time`, `created_by`, `update_time`, `updated_by`
)
VALUES (
  1, @q2_id, @note_node_id, NULL, CONCAT('seed_', UUID()),
  JSON_OBJECT('optionKeys', JSON_ARRAY('2')),
  'WRONG',
  5200,
  NOW(),
  1, NOW(), 1, NOW(), 1
);

-- 输出本次插入的题目ID，便于你确认
SELECT @note_node_id AS note_node_id, @q1_id AS q1_id, @q2_id AS q2_id, @q3_id AS q3_id;

