package org.zzt.note.data.core.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    @PostMapping
    public void add(@RequestBody NoteNodeAddDTO noteNodeAdd) {
        noteNodeDomainService.add(noteNodeAdd);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable("id") Long id, @RequestBody NoteNodeAddDTO noteNodeAdd) {
        if (noteNodeAdd == null || noteNodeAdd.getNoteNode() == null) {
            throw new IllegalArgumentException("noteNodeAdd.noteNode cannot be null");
        }
        noteNodeAdd.getNoteNode().setId(id);
        noteNodeDomainService.update(noteNodeAdd);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        NoteNodeRequest request = new NoteNodeRequest();
        request.setId(id);
        noteNodeDomainService.delete(request);
    }

    @GetMapping("/{id}")
    public NoteNodeVO get(@PathVariable("id") Long id) {
        NoteNodeRequest request = new NoteNodeRequest();
        request.setId(id);
        return noteNodeDomainService.get(request);
    }
}
