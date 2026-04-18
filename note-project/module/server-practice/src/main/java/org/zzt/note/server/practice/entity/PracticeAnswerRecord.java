package org.zzt.note.server.practice.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import org.zzt.note.server.practice.entity.converter.JsonNodeJsonConverter;
import org.zzt.note.server.practice.entity.enums.PracticeAnswerResult;

import java.time.LocalDateTime;

/**
 * 用户作答事实表
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "practice_answer_record",
        indexes = {
                @Index(name = "idx_practice_answer_record_user_time", columnList = "user_id,answered_at"),
                @Index(name = "idx_practice_answer_record_user_question", columnList = "user_id,question_id"),
                @Index(name = "idx_practice_answer_record_session_id", columnList = "session_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_practice_answer_record_user_client_req", columnNames = {"user_id", "client_request_id"})
        }
)
@ToString(callSuper = true)
public class PracticeAnswerRecord extends LogicalDeleteEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "note_node_id")
    private Long noteNodeId;

    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "client_request_id", nullable = false, length = 64)
    private String clientRequestId;

    @Convert(converter = JsonNodeJsonConverter.class)
    @Column(name = "user_answer", columnDefinition = "json")
    private JsonNode userAnswer;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false, length = 20)
    private PracticeAnswerResult result;

    @Column(name = "cost_ms")
    private Long costMs;

    @Column(name = "answered_at", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime answeredAt;
}

