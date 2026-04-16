-- note_node.note_type 从 ENUM 改为 VARCHAR，避免新增枚举值时出现 Data truncated
-- 执行前建议先备份表结构与数据

ALTER TABLE `note_node`
    MODIFY COLUMN `note_type` VARCHAR(50) NOT NULL COMMENT '笔记模板类型（NoteType 枚举名）';

