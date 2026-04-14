package org.zzt.note.server.practice.service;

import org.athena.framework.data.jdbc.vo.PageResultVO;
import org.zzt.note.server.practice.req.QuestionDomainPageRequest;
import org.zzt.note.server.practice.vo.QuestionVO;

/**
 * 题目领域服务
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
public interface IQuestionDomainService {

    QuestionVO upsert(QuestionVO questionVO);

    PageResultVO<QuestionVO> page(QuestionDomainPageRequest request);
}

