package org.zzt.note.app.layer.ops.service;

import org.zzt.note.app.layer.ops.dto.NoteNodeOpsDeleteResult;

public interface INoteNodeOpsService {

    NoteNodeOpsDeleteResult deleteWithRelations(Long noteNodeId, String checkCode);

}
