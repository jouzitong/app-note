package org.zzt.note.server.practice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zzt.note.server.practice.entity.PracticeSession;

/**
 * 练习会话仓储
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
public interface IPracticeSessionRepository extends JpaRepository<PracticeSession, Long> {
}

