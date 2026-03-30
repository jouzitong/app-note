package org.zzt.note.data.core.handler;

import org.zzt.note.data.core.type.NoteType;

/**
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
public interface INoteNodeContentHandler {

    NoteType type();

    Object getContent(Long nodeId);

}
