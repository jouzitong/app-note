package org.zzt.note.server.word.repository;

import org.athena.framework.data.jpa.repository.BaseRepository;
import org.springframework.transaction.annotation.Transactional;
import org.zzt.note.server.word.entity.WordCardNoteNodeRel;

import java.util.List;

/**
 * 单词卡片与笔记节点关联仓储
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
public interface IWordCardNoteNodeRelRepository extends BaseRepository<WordCardNoteNodeRel> {

    List<WordCardNoteNodeRel> findByWordCardId(Long wordCardId);

    List<WordCardNoteNodeRel> findByNoteNodeId(Long noteNodeId);

    @Transactional
    void deleteByWordCardId(Long wordCardId);

    @Transactional
    void deleteByNoteNodeId(Long noteNodeId);
}
