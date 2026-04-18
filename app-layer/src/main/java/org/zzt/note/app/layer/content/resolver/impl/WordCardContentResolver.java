package org.zzt.note.app.layer.content.resolver.impl;

import lombok.AllArgsConstructor;
import org.athena.framework.data.jdbc.vo.PageResultVO;
import org.springframework.stereotype.Component;
import org.zzt.note.app.layer.content.constant.NoteContentTypeConstants;
import org.zzt.note.app.layer.content.req.NoteNodeContentQuery;
import org.zzt.note.app.layer.content.resolver.INoteTypeContentResolver;
import org.zzt.note.data.core.type.NoteType;
import org.zzt.note.data.core.vo.NoteNodeVO;
import org.zzt.note.server.word.req.WordCardDomainPageRequest;
import org.zzt.note.server.word.service.IWordCardDomainService;
import org.zzt.note.server.word.vo.WordCardVO;

/**
 * WORD_CARD 内容解析。
 */
@Component
@AllArgsConstructor
public class WordCardContentResolver implements INoteTypeContentResolver {

    private final IWordCardDomainService wordCardDomainService;

    @Override
    public boolean supports(NoteType noteType) {
        return NoteType.WORD_CARD == noteType;
    }

    @Override
    public String contentType() {
        return NoteContentTypeConstants.WORD_CARD_PAGE;
    }

    @Override
    public Object resolve(NoteNodeVO noteNodeDetail, NoteNodeContentQuery query) {
        if (noteNodeDetail == null || noteNodeDetail.getNoteNode() == null || noteNodeDetail.getNoteNode().getId() == null) {
            return null;
        }

        WordCardDomainPageRequest request = new WordCardDomainPageRequest();
        request.setNoteId(noteNodeDetail.getNoteNode().getId());
        request.setUserId(query == null ? null : query.getUserId());

        if (query != null) {
            if (query.getPage() != null) {
                request.setPage(query.getPage());
            }
            if (query.getSize() != null) {
                request.setSize(query.getSize());
            }
        }

        PageResultVO<WordCardVO> result = wordCardDomainService.page(request);
        return result == null ? PageResultVO.ok() : result;
    }
}
