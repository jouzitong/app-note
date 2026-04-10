package org.zzt.note.server.practice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zzt.note.server.practice.entity.PracticeAnswerRecord;

import java.util.List;
import java.util.Optional;

/**
 * 作答事实表仓储
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
public interface IPracticeAnswerRecordRepository extends JpaRepository<PracticeAnswerRecord, Long> {

    Optional<PracticeAnswerRecord> findByUserIdAndClientRequestId(Long userId, String clientRequestId);

    Optional<PracticeAnswerRecord> findTopByUserIdAndSessionIdAndQuestionIdOrderByAnsweredAtDesc(Long userId, Long sessionId, Long questionId);

    List<PracticeAnswerRecord> findByUserIdAndQuestionId(Long userId, Long questionId);

    List<PracticeAnswerRecord> findByUserId(Long userId);

    List<PracticeAnswerRecord> findByUserIdAndNoteNodeId(Long userId, Long noteNodeId);
}
