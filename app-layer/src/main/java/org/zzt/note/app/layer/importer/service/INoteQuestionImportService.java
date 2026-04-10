package org.zzt.note.app.layer.importer.service;

import org.zzt.note.app.layer.importer.dto.NoteQuestionImportRequest;
import org.zzt.note.app.layer.importer.dto.NoteQuestionImportResult;

public interface INoteQuestionImportService {

    NoteQuestionImportResult importData(NoteQuestionImportRequest request);
}

