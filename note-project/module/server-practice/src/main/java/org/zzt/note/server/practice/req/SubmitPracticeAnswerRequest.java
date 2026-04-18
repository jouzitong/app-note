package org.zzt.note.server.practice.req;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

/**
 * 提交答案请求
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
@Data
public class SubmitPracticeAnswerRequest {
    private Integer index;
    private Long questionId;
    private Long noteNodeId;
    private String clientRequestId;
    private JsonNode userAnswer;
    private Long costMs;
    private Long userId;
}

