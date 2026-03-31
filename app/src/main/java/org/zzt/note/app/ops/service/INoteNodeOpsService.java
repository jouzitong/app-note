package org.zzt.note.app.ops.service;

import org.zzt.note.app.ops.dto.NoteNodeOpsDeleteResult;

public interface INoteNodeOpsService {

    NoteNodeOpsDeleteResult deleteWithRelations(Long noteNodeId, String checkCode);
}
