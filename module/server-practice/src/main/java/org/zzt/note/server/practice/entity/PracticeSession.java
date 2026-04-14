package org.zzt.note.server.practice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.athena.framework.data.jpa.domain.AuditableEntity;
import org.zzt.note.server.practice.entity.enums.PracticeMode;
import org.zzt.note.server.practice.entity.enums.PracticeSessionStatus;

import java.time.LocalDateTime;

/**
 * 练习会话
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "practice_session",
        indexes = {
                @Index(name = "idx_practice_session_user_time", columnList = "user_id,started_at"),
                @Index(name = "idx_practice_session_note_node_id", columnList = "note_node_id")
        }
)
@ToString(callSuper = true)
public class PracticeSession extends AuditableEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "note_node_id", nullable = false)
    private Long noteNodeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 30)
    private PracticeMode mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PracticeSessionStatus status = PracticeSessionStatus.RUNNING;

    @Column(name = "total_count", nullable = false)
    private Integer totalCount = 0;

    @Column(name = "correct_count", nullable = false)
    private Integer correctCount = 0;

    @Column(name = "wrong_count", nullable = false)
    private Integer wrongCount = 0;

    @Column(name = "answered_count", nullable = false)
    private Integer answeredCount = 0;

    @Column(name = "current_index", nullable = false)
    private Integer currentIndex = 0;

    @Column(name = "started_at", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime startedAt;

    @Column(name = "finished_at", columnDefinition = "DATETIME")
    private LocalDateTime finishedAt;
}

