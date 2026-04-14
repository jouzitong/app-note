package org.zzt.note.server.practice.vo;

import lombok.Data;

/**
 * 练习会话 VO
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
@Data
public class PracticeSessionVO {
    private Long sessionId;
    private Integer totalCount;
    private Integer currentIndex;
    private Integer answeredCount;
    private Integer correctCount;
    private Integer wrongCount;
    private Boolean finished;
}

