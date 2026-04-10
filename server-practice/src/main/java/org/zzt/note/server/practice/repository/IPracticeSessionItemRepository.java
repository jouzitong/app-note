package org.zzt.note.server.practice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zzt.note.server.practice.entity.PracticeSessionItem;

import java.util.List;
import java.util.Optional;

/**
 * 会话题目明细仓储
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
public interface IPracticeSessionItemRepository extends JpaRepository<PracticeSessionItem, Long> {

    Optional<PracticeSessionItem> findBySessionIdAndSort(Long sessionId, Integer sort);

    List<PracticeSessionItem> findBySessionIdOrderBySortAsc(Long sessionId);
}

