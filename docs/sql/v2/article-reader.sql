-- 文章阅读模块建表（MySQL 8+）

CREATE TABLE IF NOT EXISTS `article` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `article_code` VARCHAR(100) NOT NULL COMMENT '业务唯一编码（JSON id）',
    `title` VARCHAR(255) NOT NULL COMMENT '文章标题',
    `meta_info` JSON NULL COMMENT '文章内容（paragraphs + translation）',
    `created_by` BIGINT NULL,
    `created_at` DATETIME NULL,
    `last_modified_by` BIGINT NULL,
    `last_modified_at` DATETIME NULL,
    `is_deleted` BIGINT NULL,
    `deleted_at` DATETIME NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_article_code` (`article_code`),
    KEY `idx_article_article_code` (`article_code`),
    KEY `idx_article_title` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='阅读文章';

CREATE TABLE IF NOT EXISTS `article_note_node_rel` (
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `note_node_id` BIGINT NOT NULL COMMENT '笔记节点ID',
    PRIMARY KEY (`article_id`, `note_node_id`),
    KEY `idx_article_note_node_rel_article_id` (`article_id`),
    KEY `idx_article_note_node_rel_note_node_id` (`note_node_id`),
    CONSTRAINT `fk_article_note_node_rel_article`
        FOREIGN KEY (`article_id`) REFERENCES `article` (`id`),
    CONSTRAINT `fk_article_note_node_rel_note_node`
        FOREIGN KEY (`note_node_id`) REFERENCES `note_node` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章与笔记节点关联';

CREATE TABLE IF NOT EXISTS `article_user_progress` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `favorite` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否收藏',
    `completed` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否完成',
    `last_read_paragraph_index` INT NULL COMMENT '最近阅读段落下标（从0开始）',
    `playback_rate` DECIMAL(4,2) NULL DEFAULT 1.00 COMMENT '播放速度',
    `created_by` BIGINT NULL,
    `created_at` DATETIME NULL,
    `last_modified_by` BIGINT NULL,
    `last_modified_at` DATETIME NULL,
    `is_deleted` BIGINT NULL,
    `deleted_at` DATETIME NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_user_progress_user_article` (`user_id`, `article_id`),
    KEY `idx_article_user_progress_user_id` (`user_id`),
    KEY `idx_article_user_progress_article_id` (`article_id`),
    CONSTRAINT `fk_article_user_progress_article`
        FOREIGN KEY (`article_id`) REFERENCES `article` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户文章阅读进度';

-- 示例数据：refer/node/文章/article-data.json
INSERT INTO `article` (`article_code`, `title`, `meta_info`, `created_by`, `created_at`, `last_modified_by`, `last_modified_at`)
VALUES (
    'article_001',
    '私の毎日の生活',
    JSON_OBJECT(
        'paragraphs', JSON_ARRAY(
            JSON_ARRAY(
                JSON_OBJECT('text', '私', 'kana', 'わたし'),
                JSON_OBJECT('text', 'は'),
                JSON_OBJECT('text', '毎朝', 'kana', 'まいあさ'),
                JSON_OBJECT('text', '七時', 'kana', 'しちじ'),
                JSON_OBJECT('text', 'に'),
                JSON_OBJECT('text', '起', 'kana', 'お'),
                JSON_OBJECT('text', 'きます。')
            ),
            JSON_ARRAY(
                JSON_OBJECT('text', '朝ご飯', 'kana', 'あさごはん'),
                JSON_OBJECT('text', 'を'),
                JSON_OBJECT('text', '食', 'kana', 'た'),
                JSON_OBJECT('text', 'べて、'),
                JSON_OBJECT('text', '学校', 'kana', 'がっこう'),
                JSON_OBJECT('text', 'へ'),
                JSON_OBJECT('text', '行', 'kana', 'い'),
                JSON_OBJECT('text', 'きます。')
            )
        ),
        'translation', JSON_ARRAY(
            '我每天早上七点起床。',
            '吃完早饭去学校。'
        ),
        'knowledge', JSON_OBJECT(
            'coreVocabulary', JSON_ARRAY(
                JSON_OBJECT('jp', '毎朝', 'kana', 'まいあさ', 'meaning', '每天早晨'),
                JSON_OBJECT('jp', '朝ご飯', 'kana', 'あさごはん', 'meaning', '早饭')
            ),
            'coreSentencePatterns', JSON_ARRAY(
                JSON_OBJECT('jp', '〜は 〜に 〜ます', 'meaning', '在某时间做某事'),
                JSON_OBJECT('jp', '〜を 〜て、〜ます', 'meaning', '动作用て形连接，表示先后顺序')
            )
        )
    ),
    1,
    NOW(),
    1,
    NOW()
)
ON DUPLICATE KEY UPDATE
    `title` = VALUES(`title`),
    `meta_info` = VALUES(`meta_info`),
    `last_modified_by` = VALUES(`last_modified_by`),
    `last_modified_at` = VALUES(`last_modified_at`);
