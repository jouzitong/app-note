package org.zzt.note.app.layer.importer.service;

import org.zzt.note.app.layer.importer.dto.NoteArticleImportRequest;
import org.zzt.note.app.layer.importer.dto.NoteArticleImportResult;

public interface INoteArticleImportService {

    NoteArticleImportResult importData(NoteArticleImportRequest request);

}
