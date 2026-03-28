package org.zzt.note.data.core.controller;

import org.athena.framework.data.jdbc.req.BaseRequest;
import org.athena.framework.data.jdbc.web.BaseControllerV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zzt.note.data.core.entity.NoteNode;
import org.zzt.note.data.core.entity.dto.NoteNodeDTO;
import org.zzt.note.data.core.service.INoteNodeService;

/**
 *
 * @author zhouzhitong
 * @since 2026/3/27
 */
@RestController
@RequestMapping("/api/v1/noteNodes")
public class NoteNodeController extends BaseControllerV2<NoteNode, NoteNodeDTO, BaseRequest, INoteNodeService> {

    @Autowired
    private INoteNodeService noteNodeService;

    @Override
    protected INoteNodeService service() {
        return noteNodeService;
    }
}
