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
import org.zzt.note.app.layer.importer.dto.NoteArticleImportRequest;
import org.zzt.note.app.layer.importer.dto.NoteArticleImportResult;
import org.zzt.note.app.layer.importer.service.INoteArticleImportService;

import java.io.IOException;

/**
 * 文章导入接口。
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/imports/note-article")
public class NoteArticleImportController {

    private final INoteArticleImportService importService;

    private final ObjectMapper objectMapper;

    @PostMapping
    public NoteArticleImportResult importData(@RequestBody NoteArticleImportRequest request) {
        return importService.importData(request);
    }

    @PostMapping(value = "/json-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public NoteArticleImportResult importDataByJsonFile(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("json file cannot be empty");
        }
        try {
            NoteArticleImportRequest request = objectMapper.readValue(file.getInputStream(), NoteArticleImportRequest.class);
            return importService.importData(request);
        } catch (IOException e) {
            throw new IllegalArgumentException("invalid json file content", e);
        }
    }
}
