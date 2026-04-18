package org.zzt.note.app.layer.content.resolver;

import org.zzt.note.app.layer.content.req.NoteNodeContentQuery;
import org.zzt.note.data.core.type.NoteType;
import org.zzt.note.data.core.vo.NoteNodeVO;

/**
 * 按 noteType 解析内容。
 */
public interface INoteTypeContentResolver {

    boolean supports(NoteType noteType);

    String contentType();

    Object resolve(NoteNodeVO noteNodeDetail, NoteNodeContentQuery query);
}
