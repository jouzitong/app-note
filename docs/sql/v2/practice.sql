-- 做题/练习模块建表（MySQL 8+）

CREATE TABLE IF NOT EXISTS `question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `version` BIGINT NOT NULL DEFAULT 1 COMMENT '版本',
    `question_code` VARCHAR(100) NOT NULL COMMENT '业务唯一键（导入/幂等）',
    `title` VARCHAR(255) NOT NULL COMMENT '题干标题/简述',
    `stem` TEXT NOT NULL COMMENT '题干正文',
    `question_type` VARCHAR(30) NOT NULL COMMENT '题型',
    `difficulty` INT NULL COMMENT '难度',
    `locale` VARCHAR(20) NULL COMMENT '语言环境',
    `meta_info` JSON NULL COMMENT '题目结构载荷（options/answerKey/analysis/assets/display/ext）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `created_by` BIGINT NULL DEFAULT -1 COMMENT '创建人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后修改时间',
    `updated_by` BIGINT NULL DEFAULT -1 COMMENT '最后修改人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_question_question_code` (`question_code`),
    KEY `idx_question_question_code` (`question_code`),
    KEY `idx_question_title` (`title`),
    KEY `idx_question_type` (`question_type`),
    KEY `idx_question_locale` (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='题目';

CREATE TABLE IF NOT EXISTS `question_note_node_rel` (
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `note_node_id` BIGINT NOT NULL COMMENT '笔记节点ID',
    PRIMARY KEY (`question_id`, `note_node_id`),
    KEY `idx_question_note_node_rel_question_id` (`question_id`),
    KEY `idx_question_note_node_rel_note_node_id` (`note_node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='题目与笔记节点关联';

CREATE TABLE IF NOT EXISTS `user_question_progress` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `version` BIGINT NOT NULL DEFAULT 1 COMMENT '版本',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `status` VARCHAR(30) NOT NULL COMMENT '题目状态',
    `wrong_count` INT NOT NULL DEFAULT 0 COMMENT '累计错误次数',
    `correct_count` INT NOT NULL DEFAULT 0 COMMENT '累计正确次数',
    `correct_streak` INT NOT NULL DEFAULT 0 COMMENT '连续正确次数',
    `last_answered_at` DATETIME NULL COMMENT '最近作答时间',
    `last_wrong_at` DATETIME NULL COMMENT '最近错误时间',
    `last_correct_at` DATETIME NULL COMMENT '最近正确时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `created_by` BIGINT NULL DEFAULT -1 COMMENT '创建人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后修改时间',
    `updated_by` BIGINT NULL DEFAULT -1 COMMENT '最后修改人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_question_progress_user_question` (`user_id`, `question_id`),
    KEY `idx_user_question_progress_user_id` (`user_id`),
    KEY `idx_user_question_progress_status` (`status`),
    KEY `idx_user_question_progress_last_answered_at` (`last_answered_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户题目进度/错题本';

CREATE TABLE IF NOT EXISTS `practice_session` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `version` BIGINT NOT NULL DEFAULT 1 COMMENT '版本',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `note_node_id` BIGINT NOT NULL COMMENT '练习入口节点ID',
    `mode` VARCHAR(30) NOT NULL COMMENT '会话模式',
    `status` VARCHAR(20) NOT NULL COMMENT '会话状态',
    `total_count` INT NOT NULL DEFAULT 0 COMMENT '题目总数',
    `correct_count` INT NOT NULL DEFAULT 0 COMMENT '正确数',
    `wrong_count` INT NOT NULL DEFAULT 0 COMMENT '错误数',
    `answered_count` INT NOT NULL DEFAULT 0 COMMENT '已作答数',
    `current_index` INT NOT NULL DEFAULT 0 COMMENT '当前下标（0-based）',
    `started_at` DATETIME NOT NULL COMMENT '开始时间',
    `finished_at` DATETIME NULL COMMENT '结束时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `created_by` BIGINT NULL DEFAULT -1 COMMENT '创建人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后修改时间',
    `updated_by` BIGINT NULL DEFAULT -1 COMMENT '最后修改人',
    PRIMARY KEY (`id`),
    KEY `idx_practice_session_user_time` (`user_id`, `started_at`),
    KEY `idx_practice_session_note_node_id` (`note_node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='练习会话';

CREATE TABLE IF NOT EXISTS `practice_session_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `version` BIGINT NOT NULL DEFAULT 1 COMMENT '版本',
    `session_id` BIGINT NOT NULL COMMENT '会话ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '题目顺序',
    `answered` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已作答',
    `result` VARCHAR(20) NULL COMMENT '作答结果（冗余）',
    `answered_at` DATETIME NULL COMMENT '作答时间（冗余）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `created_by` BIGINT NULL DEFAULT -1 COMMENT '创建人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后修改时间',
    `updated_by` BIGINT NULL DEFAULT -1 COMMENT '最后修改人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_practice_session_item` (`session_id`, `sort`),
    KEY `idx_practice_session_item_session_sort` (`session_id`, `sort`),
    KEY `idx_practice_session_item_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='练习会话题目明细';

CREATE TABLE IF NOT EXISTS `practice_answer_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `version` BIGINT NOT NULL DEFAULT 1 COMMENT '版本',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `note_node_id` BIGINT NULL COMMENT '节点上下文',
    `session_id` BIGINT NULL COMMENT '会话ID',
    `client_request_id` VARCHAR(64) NOT NULL COMMENT '客户端幂等键',
    `user_answer` JSON NULL COMMENT '用户答案',
    `result` VARCHAR(20) NOT NULL COMMENT '作答结果',
    `cost_ms` BIGINT NULL COMMENT '耗时（毫秒）',
    `answered_at` DATETIME NOT NULL COMMENT '作答时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `created_by` BIGINT NULL DEFAULT -1 COMMENT '创建人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后修改时间',
    `updated_by` BIGINT NULL DEFAULT -1 COMMENT '最后修改人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_practice_answer_record_user_client_req` (`user_id`, `client_request_id`),
    KEY `idx_practice_answer_record_user_time` (`user_id`, `answered_at`),
    KEY `idx_practice_answer_record_user_question` (`user_id`, `question_id`),
    KEY `idx_practice_answer_record_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户作答事实表';

