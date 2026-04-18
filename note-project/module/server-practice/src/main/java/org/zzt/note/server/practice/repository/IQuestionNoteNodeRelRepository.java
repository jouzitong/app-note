package org.zzt.note.server.practice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.zzt.note.server.practice.entity.QuestionNoteNodeRel;
import org.zzt.note.server.practice.entity.QuestionNoteNodeRelId;

import java.util.List;

/**
 * 题目与节点关联仓储
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
public interface IQuestionNoteNodeRelRepository extends JpaRepository<QuestionNoteNodeRel, QuestionNoteNodeRelId> {

    List<QuestionNoteNodeRel> findByQuestionId(Long questionId);

    List<QuestionNoteNodeRel> findByNoteNodeIdOrderByQuestionIdAsc(Long noteNodeId);

    Page<QuestionNoteNodeRel> findByNoteNodeIdOrderByQuestionIdAsc(Long noteNodeId, Pageable pageable);

    void deleteByQuestionId(Long questionId);
}
