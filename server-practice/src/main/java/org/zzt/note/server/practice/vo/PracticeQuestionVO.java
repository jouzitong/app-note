package org.zzt.note.server.practice.vo;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import org.zzt.note.server.practice.entity.enums.PracticeAnswerResult;
import org.zzt.note.server.practice.entity.enums.QuestionType;
import org.zzt.note.server.practice.entity.meta.QuestionMetaInfo;

/**
 * 会话题目 VO（脱敏后的展示模型）
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
@Data
public class PracticeQuestionVO {
    private Long questionId;
    private QuestionType questionType;
    private String title;
    private String stem;
    private QuestionMetaInfo metaInfo;

    private JsonNode userAnswer;
    private PracticeAnswerResult result;
}

