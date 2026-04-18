package org.zzt.note.app.layer.content.resolver.impl;

import lombok.AllArgsConstructor;
import org.athena.framework.data.jdbc.vo.PageResultVO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.zzt.note.app.layer.content.constant.NoteContentTypeConstants;
import org.zzt.note.app.layer.content.req.NoteNodeContentQuery;
import org.zzt.note.app.layer.content.resolver.INoteTypeContentResolver;
import org.zzt.note.data.core.type.NoteType;
import org.zzt.note.data.core.vo.NoteNodeVO;
import org.zzt.note.server.practice.entity.enums.QuestionType;
import org.zzt.note.server.practice.req.QuestionDomainPageRequest;
import org.zzt.note.server.practice.service.IQuestionDomainService;
import org.zzt.note.server.practice.vo.QuestionVO;

/**
 * QUESTIONS 内容解析。
 */
@Component
@AllArgsConstructor
public class QuestionContentResolver implements INoteTypeContentResolver {

    private final IQuestionDomainService questionDomainService;

    @Override
    public boolean supports(NoteType noteType) {
        return NoteType.PRACTICE == noteType;
    }

    @Override
    public String contentType() {
        return NoteContentTypeConstants.QUESTION_PAGE;
    }

    @Override
    public Object resolve(NoteNodeVO noteNodeDetail, NoteNodeContentQuery query) {
        if (noteNodeDetail == null || noteNodeDetail.getNoteNode() == null || noteNodeDetail.getNoteNode().getId() == null) {
            return null;
        }

        QuestionDomainPageRequest request = new QuestionDomainPageRequest();
        request.setNoteNodeId(noteNodeDetail.getNoteNode().getId());

        if (query != null) {
            if (query.getPage() != null) {
                request.setPage(query.getPage());
            }
            if (query.getSize() != null) {
                request.setSize(query.getSize());
            }
            if (StringUtils.hasText(query.getQuestionType())) {
                try {
                    request.setQuestionType(QuestionType.valueOf(query.getQuestionType().trim().toUpperCase()));
                } catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException("Unsupported questionType: " + query.getQuestionType());
                }
            }
        }

        PageResultVO<QuestionVO> result = questionDomainService.page(request);
        return result == null ? PageResultVO.ok() : result;
    }
}
