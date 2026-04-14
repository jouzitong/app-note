package org.zzt.note.server.practice.vo;

import lombok.Data;

/**
 * 获取指定题目响应
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
@Data
public class GetPracticeItemResponse {
    private PracticeSessionVO session;
    private PracticeQuestionVO question;
}

