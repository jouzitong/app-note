package org.zzt.note.app.layer.ops.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zzt.note.app.layer.ops.dto.NoteNodeOpsDeleteResult;
import org.zzt.note.app.layer.ops.service.INoteNodeOpsService;

/**
 * 节点运维接口。
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/ops/noteNodes")
public class NoteNodeOpsController {

    private final INoteNodeOpsService noteNodeOpsService;

    @PostMapping("/delete")
    public NoteNodeOpsDeleteResult delete(@RequestParam("noteNodeId") Long noteNodeId,
                                          @RequestParam("checkCode") String checkCode) {
        return noteNodeOpsService.deleteWithRelations(noteNodeId, checkCode);
    }
}
