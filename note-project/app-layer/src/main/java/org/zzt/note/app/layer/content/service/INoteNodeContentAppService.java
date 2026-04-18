package org.zzt.note.app.layer.content.service;

import org.zzt.note.app.layer.content.req.NoteNodeContentQuery;
import org.zzt.note.app.layer.content.vo.NoteNodeContentAppVO;

public interface INoteNodeContentAppService {

    NoteNodeContentAppVO getByNoteNodeId(Long noteNodeId, NoteNodeContentQuery query);
}
