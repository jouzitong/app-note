package org.zzt.note.app.layer.importer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.zzt.note.app.layer.importer.dto.NoteQuestionImportRequest;
import org.zzt.note.app.layer.importer.dto.NoteQuestionImportResult;
import org.zzt.note.app.layer.importer.service.INoteQuestionImportService;

import java.io.IOException;

/**
 * 笔记节点 + 题库导入接口。
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/imports/note-question")
public class NoteQuestionImportController {

    private final INoteQuestionImportService importService;

    private final ObjectMapper objectMapper;

    @PostMapping
    public NoteQuestionImportResult importData(@RequestBody NoteQuestionImportRequest request) {
        return importService.importData(request);
    }

    @PostMapping(value = "/json-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public NoteQuestionImportResult importDataByJsonFile(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("json file cannot be empty");
        }
        try {
            NoteQuestionImportRequest request = objectMapper.readValue(file.getInputStream(), NoteQuestionImportRequest.class);
            return importService.importData(request);
        } catch (IOException e) {
            throw new IllegalArgumentException("invalid json file content", e);
        }
    }
}

