package org.zzt.note.server.practice.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 节点练习统计
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
@Data
public class NoteNodePracticeStatsVO {
    private Long noteNodeId;
    private Long totalQuestionCount;
    private Long answeredCount;
    private Long wrongCount;
    private Long masteredCount;
    private LocalDateTime lastAnsweredAt;
}

