package org.zzt.note.server.practice.vo;

import lombok.Data;
import org.zzt.note.server.practice.entity.enums.QuestionType;
import org.zzt.note.server.practice.entity.meta.QuestionMetaInfo;

import java.util.List;

/**
 * 题目 VO
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
@Data
public class QuestionVO {
    private Long id;
    private String questionCode;
    private String title;
    private String stem;
    private QuestionType questionType;
    private Integer difficulty;
    private String locale;
    private QuestionMetaInfo metaInfo;

    /**
     * 题目绑定的节点ID（写入时使用）
     */
    private List<Long> bindNoteNodeIds;
}

