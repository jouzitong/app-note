package org.zzt.note.data.core.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zzt.note.data.core.dto.NoteNodeAddDTO;
import org.zzt.note.data.core.request.NoteNodeRequest;
import org.zzt.note.data.core.service.INoteNodeDomainService;
import org.zzt.note.data.core.vo.NoteNodeVO;

/**
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/noteNodes/domain")
public class NoteNodeDomainController {

    private final INoteNodeDomainService noteNodeDomainService;

    @PostMapping("/add")
    public void add(@RequestBody NoteNodeAddDTO noteNodeAdd) {
        noteNodeDomainService.add(noteNodeAdd);
    }

    @PostMapping("/get")
    public NoteNodeVO get(@RequestBody NoteNodeRequest request) {
        return noteNodeDomainService.get(request);
    }
}
