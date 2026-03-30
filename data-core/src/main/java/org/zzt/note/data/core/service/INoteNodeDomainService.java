package org.zzt.note.data.core.service;

import org.zzt.note.data.core.dto.NoteNodeAddDTO;
import org.zzt.note.data.core.request.NoteNodeRequest;
import org.zzt.note.data.core.vo.NoteNodeVO;

/**
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
public interface INoteNodeDomainService {

    void add(NoteNodeAddDTO noteNodeAdd);

    void update(NoteNodeAddDTO noteNodeAdd);

    void delete(NoteNodeRequest request);

    NoteNodeVO get(NoteNodeRequest request);

}
