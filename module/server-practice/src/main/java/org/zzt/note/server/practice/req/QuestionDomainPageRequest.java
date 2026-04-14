package org.zzt.note.server.practice.req;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.athena.framework.data.jdbc.req.BaseRequest;
import org.zzt.note.server.practice.entity.enums.QuestionType;

/**
 * 题目领域分页请求
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionDomainPageRequest extends BaseRequest {

    /**
     * 笔记节点ID
     */
    private Long noteNodeId;

    /**
     * 题型（可选）
     */
    private QuestionType questionType;

    /**
     * 关键字（可选：标题/题干）
     */
    private String keyword;
}

