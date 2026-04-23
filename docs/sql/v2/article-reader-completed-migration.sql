-- article_user_progress 增加 completed 字段
ALTER TABLE `article_user_progress`
    ADD COLUMN `completed` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否完成' AFTER `favorite`;

-- 历史数据回填（理论上默认值已覆盖，保留显式语句便于审计）
UPDATE `article_user_progress`
SET `completed` = 0
WHERE `completed` IS NULL;
