package org.zzt.note.app.layer.content.resolver.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.zzt.note.app.layer.content.constant.NoteContentTypeConstants;
import org.zzt.note.app.layer.content.req.NoteNodeContentQuery;
import org.zzt.note.app.layer.content.resolver.INoteTypeContentResolver;
import org.zzt.note.data.core.type.NoteType;
import org.zzt.note.data.core.vo.NoteNodeVO;
import org.zzt.note.server.word.article.service.IArticleDomainService;

/**
 * ARTICLE 内容解析。
 */
@Component
@AllArgsConstructor
public class ArticleContentResolver implements INoteTypeContentResolver {

    private final IArticleDomainService articleDomainService;

    @Override
    public boolean supports(NoteType noteType) {
        return NoteType.ARTICLE == noteType;
    }

    @Override
    public String contentType() {
        return NoteContentTypeConstants.ARTICLE_DETAIL;
    }

    @Override
    public Object resolve(NoteNodeVO noteNodeDetail, NoteNodeContentQuery query) {
        if (noteNodeDetail == null || noteNodeDetail.getNoteNode() == null || noteNodeDetail.getNoteNode().getId() == null) {
            return null;
        }
        Long noteNodeId = noteNodeDetail.getNoteNode().getId();
        try {
            return articleDomainService.getByNoteNodeId(noteNodeId);
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("not found")) {
                return null;
            }
            throw ex;
        }
    }
}
