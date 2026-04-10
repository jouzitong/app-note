package org.zzt.note.server.practice.vo;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import org.zzt.note.server.practice.entity.enums.PracticeAnswerResult;

import java.util.Map;

/**
 * 提交答案响应
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
@Data
public class SubmitPracticeAnswerResponse {
    private PracticeAnswerResult result;
    private Map<String, Object> correctAnswer;
    private Map<String, Object> analysis;
    private PracticeSessionVO session;
    private PracticeQuestionVO nextQuestion;
    private JsonNode userAnswer;
}

