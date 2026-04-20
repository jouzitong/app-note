package org.zzt.note.app.layer.content.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.zzt.note.app.layer.content.constant.NoteContentTypeConstants;
import org.zzt.note.app.layer.content.req.NoteNodeContentQuery;
import org.zzt.note.app.layer.content.resolver.INoteTypeContentResolver;
import org.zzt.note.app.layer.content.service.INoteNodeContentAppService;
import org.zzt.note.app.layer.content.vo.NoteNodeContentAppVO;
import org.zzt.note.data.core.request.NoteNodeRequest;
import org.zzt.note.data.core.service.INoteNodeDomainService;
import org.zzt.note.data.core.type.NoteType;
import org.zzt.note.data.core.vo.NoteNodeVO;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 节点内容 app 聚合服务。
 */
@Service
@AllArgsConstructor
public class NoteNodeContentAppServiceImpl implements INoteNodeContentAppService {

    private final INoteNodeDomainService noteNodeDomainService;

    private final List<INoteTypeContentResolver> contentResolvers;

    @Override
    public NoteNodeContentAppVO getByNoteNodeId(Long noteNodeId, NoteNodeContentQuery query) {
        if (noteNodeId == null) {
            throw new IllegalArgumentException("noteNodeId cannot be null");
        }

        NoteNodeRequest request = new NoteNodeRequest();
        request.setId(noteNodeId);
        NoteNodeVO noteNodeDetail = noteNodeDomainService.get(request);

        NoteNodeContentAppVO vo = new NoteNodeContentAppVO();
        vo.setNoteNode(noteNodeDetail.getNoteNode());
        vo.setPaths(noteNodeDetail.getPaths());
        vo.setChildNoteNodes(Boolean.FALSE.equals(query == null ? null : query.getIncludeChildren())
                ? List.of()
                : noteNodeDetail.getChildNoteNodes());
        vo.setNoteType(noteNodeDetail.getNoteNode() == null || noteNodeDetail.getNoteNode().getNoteType() == null
                ? null
                : noteNodeDetail.getNoteNode().getNoteType().name());
        vo.getExt().put("query", toQueryEcho(query));

        NoteType noteType = resolveNoteType(noteNodeDetail);
        vo.getExt().put("resolvedNoteType", noteType == null ? null : noteType.name());
        if (noteType == null || NoteType.MARKDOWN == noteType || NoteType.EMPTY == noteType) {
            vo.setContentType(NoteContentTypeConstants.NOTE_NODE_CONTENT);
            vo.setContent(noteNodeDetail.getContent());
            return vo;
        }

        INoteTypeContentResolver resolver = pickResolver(noteType);
        if (resolver == null) {
            vo.setContentType(NoteContentTypeConstants.UNSUPPORTED);
            vo.setContent(noteNodeDetail.getContent());
            vo.getExt().put("message", "Unsupported noteType: " + vo.getNoteType());
            return vo;
        }

        Object content = resolver.resolve(noteNodeDetail, query);
        vo.setContentType(resolver.contentType());
        vo.setContent(content);
        if (content == null) {
            vo.getExt().put("empty", true);
        }
        return vo;
    }

    private INoteTypeContentResolver pickResolver(NoteType noteType) {
        if (CollectionUtils.isEmpty(contentResolvers)) {
            return null;
        }
        for (INoteTypeContentResolver resolver : contentResolvers) {
            if (resolver.supports(noteType)) {
                return resolver;
            }
        }
        return null;
    }

    private NoteType resolveNoteType(NoteNodeVO noteNodeDetail) {
        if (noteNodeDetail == null || noteNodeDetail.getNoteNode() == null) {
            return null;
        }
        return noteNodeDetail.getNoteNode().getNoteType();
    }

    private Map<String, Object> toQueryEcho(NoteNodeContentQuery query) {
        Map<String, Object> echo = new LinkedHashMap<>();
        if (query == null) {
            return echo;
        }
        echo.put("page", query.getPage());
        echo.put("size", query.getSize());
        echo.put("index", query.getIndex());
        echo.put("userId", query.getUserId());
        echo.put("questionType", query.getQuestionType());
        echo.put("includeChildren", query.getIncludeChildren());
        return echo;
    }
}
