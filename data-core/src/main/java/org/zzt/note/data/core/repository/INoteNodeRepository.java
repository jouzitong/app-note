package org.zzt.note.data.core.repository;

import org.athena.framework.data.jpa.repository.BaseRepository;
import org.zzt.note.data.core.entity.NoteNode;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author zhouzhitong
 * @since 2026/3/27
 */
public interface INoteNodeRepository extends BaseRepository<NoteNode> {

    Optional<NoteNode> findByNoteKey(String noteKey);

    List<NoteNode> findByIdIn(List<Long> ids);

    List<NoteNode> findByParentIdOrderBySortAsc(Long parentId);
}
