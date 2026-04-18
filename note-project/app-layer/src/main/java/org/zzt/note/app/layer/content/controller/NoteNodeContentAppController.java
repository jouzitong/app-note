package org.zzt.note.app.layer.content.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zzt.note.app.layer.content.req.NoteNodeContentQuery;
import org.zzt.note.app.layer.content.service.INoteNodeContentAppService;
import org.zzt.note.app.layer.content.vo.NoteNodeContentAppVO;

/**
 * 节点内容 app 聚合接口。
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/app/noteNodes")
public class NoteNodeContentAppController {

    private final INoteNodeContentAppService noteNodeContentAppService;

    @GetMapping("/{noteNodeId}/content")
    public NoteNodeContentAppVO get(@PathVariable("noteNodeId") Long noteNodeId,
                                    NoteNodeContentQuery query) {
        return noteNodeContentAppService.getByNoteNodeId(noteNodeId, query);
    }
}
