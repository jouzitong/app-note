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
import org.athena.framework.data.jpa.domain.AuditableEntity;
import org.zzt.note.server.practice.entity.enums.PracticeAnswerResult;

import java.time.LocalDateTime;

/**
 * 练习会话题目明细
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "practice_session_item",
        indexes = {
                @Index(name = "idx_practice_session_item_session_sort", columnList = "session_id,sort"),
                @Index(name = "idx_practice_session_item_question_id", columnList = "question_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_practice_session_item", columnNames = {"session_id", "sort"})
        }
)
@ToString(callSuper = true)
public class PracticeSessionItem extends AuditableEntity {

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "sort", nullable = false)
    private Integer sort = 0;

    @Column(name = "answered", nullable = false)
    private Boolean answered = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", length = 20)
    private PracticeAnswerResult result;

    @Column(name = "answered_at", columnDefinition = "DATETIME")
    private LocalDateTime answeredAt;
}

