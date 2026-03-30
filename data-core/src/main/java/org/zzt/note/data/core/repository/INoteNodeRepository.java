package org.zzt.note.data.core.repository;

import org.athena.framework.data.jpa.repository.BaseRepository;
import org.zzt.note.data.core.entity.NoteNode;

import java.util.List;

/**
 *
 * @author zhouzhitong
 * @since 2026/3/27
 */
public interface INoteNodeRepository extends BaseRepository<NoteNode> {

    List<NoteNode> findByIdIn(List<Long> ids);

    List<NoteNode> findByParentIdOrderBySortAsc(Long parentId);
}
