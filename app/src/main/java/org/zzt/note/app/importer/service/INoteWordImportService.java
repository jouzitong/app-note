package org.zzt.note.app.importer.service;

import org.zzt.note.app.importer.dto.NoteWordImportRequest;
import org.zzt.note.app.importer.dto.NoteWordImportResult;

public interface INoteWordImportService {

    NoteWordImportResult importData(NoteWordImportRequest request);
}
