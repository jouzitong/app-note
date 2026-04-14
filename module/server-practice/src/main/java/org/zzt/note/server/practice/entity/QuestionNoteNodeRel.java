package org.zzt.note.server.practice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 题目与笔记节点关联表
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@IdClass(QuestionNoteNodeRelId.class)
@Table(
        name = "question_note_node_rel",
        indexes = {
                @Index(name = "idx_question_note_node_rel_question_id", columnList = "question_id"),
                @Index(name = "idx_question_note_node_rel_note_node_id", columnList = "note_node_id")
        }
)
@ToString
public class QuestionNoteNodeRel {

    @Id
    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Id
    @Column(name = "note_node_id", nullable = false)
    private Long noteNodeId;
}

