package org.zzt.note.server.practice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.athena.framework.data.jpa.domain.LogicalDeleteEntity;
import org.zzt.note.server.practice.entity.enums.UserQuestionProgressStatus;

import java.time.LocalDateTime;

/**
 * 用户题目进度/错题本
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "user_question_progress",
        indexes = {
                @Index(name = "idx_user_question_progress_user_id", columnList = "user_id"),
                @Index(name = "idx_user_question_progress_status", columnList = "status"),
                @Index(name = "idx_user_question_progress_last_answered_at", columnList = "last_answered_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_question_progress_user_question", columnNames = {"user_id", "question_id"})
        }
)
@ToString(callSuper = true)
public class UserQuestionProgress extends LogicalDeleteEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private UserQuestionProgressStatus status = UserQuestionProgressStatus.NEW;

    @Column(name = "wrong_count", nullable = false)
    private Integer wrongCount = 0;

    @Column(name = "correct_count", nullable = false)
    private Integer correctCount = 0;

    @Column(name = "correct_streak", nullable = false)
    private Integer correctStreak = 0;

    @Column(name = "last_answered_at", columnDefinition = "DATETIME")
    private LocalDateTime lastAnsweredAt;

    @Column(name = "last_wrong_at", columnDefinition = "DATETIME")
    private LocalDateTime lastWrongAt;

    @Column(name = "last_correct_at", columnDefinition = "DATETIME")
    private LocalDateTime lastCorrectAt;
}

