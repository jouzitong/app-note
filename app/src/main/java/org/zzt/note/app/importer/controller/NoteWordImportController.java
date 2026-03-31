package org.zzt.note.app.importer.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zzt.note.app.importer.dto.NoteWordImportRequest;
import org.zzt.note.app.importer.dto.NoteWordImportResult;
import org.zzt.note.app.importer.service.INoteWordImportService;

/**
 * 笔记节点 + 单词卡导入接口
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/imports/note-word")
public class NoteWordImportController {

    private final INoteWordImportService importService;

    @PostMapping
    public NoteWordImportResult importData(@RequestBody NoteWordImportRequest request) {
        return importService.importData(request);
    }
}
