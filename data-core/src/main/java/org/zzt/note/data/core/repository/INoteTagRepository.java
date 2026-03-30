package org.zzt.note.data.core.repository;

import org.athena.framework.data.jpa.repository.BaseRepository;
import org.zzt.note.data.core.entity.NoteTag;

import java.util.List;
import java.util.Optional;

/**
 * 标签仓储
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
public interface INoteTagRepository extends BaseRepository<NoteTag> {

    Optional<NoteTag> findByBizTypeAndLabel(String bizType, String label);

    List<NoteTag> findByBizTypeAndLabelIn(String bizType, List<String> labels);
}
