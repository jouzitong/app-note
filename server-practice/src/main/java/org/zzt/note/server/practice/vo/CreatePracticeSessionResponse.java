package org.zzt.note.server.practice.vo;

import lombok.Data;

/**
 * 创建会话响应
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
@Data
public class CreatePracticeSessionResponse {
    private PracticeSessionVO session;
    private PracticeQuestionVO question;
}

