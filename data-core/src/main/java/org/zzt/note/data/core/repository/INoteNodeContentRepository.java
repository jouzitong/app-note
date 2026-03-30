package org.zzt.note.data.core.repository;

import org.athena.framework.data.jpa.repository.BaseRepository;
import org.zzt.note.data.core.entity.NoteNodeContent;

import java.util.Optional;

/**
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
public interface INoteNodeContentRepository extends BaseRepository<NoteNodeContent> {

    Optional<NoteNodeContent> findByNodeId(Long nodeId);
}
