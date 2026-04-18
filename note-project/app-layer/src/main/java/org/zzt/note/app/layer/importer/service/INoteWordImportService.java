package org.zzt.note.app.layer.importer.service;

import org.zzt.note.app.layer.importer.dto.NoteWordImportRequest;
import org.zzt.note.app.layer.importer.dto.NoteWordImportResult;

public interface INoteWordImportService {

    NoteWordImportResult importData(NoteWordImportRequest request);

}
