package org.zzt.note.app.importer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.zzt.note.app.importer.dto.NoteWordImportRequest;
import org.zzt.note.app.importer.dto.NoteWordImportResult;
import org.zzt.note.app.importer.service.INoteWordImportService;

import java.io.IOException;

/**
 * 笔记节点 + 单词卡导入接口
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/imports/note-word")
public class NoteWordImportController {

    private final INoteWordImportService importService;

    private final ObjectMapper objectMapper;

    @PostMapping
    public NoteWordImportResult importData(@RequestBody NoteWordImportRequest request) {
        return importService.importData(request);
    }

    @PostMapping(value = "/json-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public NoteWordImportResult importDataByJsonFile(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("json file cannot be empty");
        }
        try {
            NoteWordImportRequest request = objectMapper.readValue(file.getInputStream(), NoteWordImportRequest.class);
            return importService.importData(request);
        } catch (IOException e) {
            throw new IllegalArgumentException("invalid json file content", e);
        }
    }
}
