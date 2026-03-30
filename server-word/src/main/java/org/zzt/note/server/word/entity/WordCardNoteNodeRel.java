package org.zzt.note.server.word.entity;

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
 * 单词卡片与笔记节点关联表
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@IdClass(WordCardNoteNodeRelId.class)
@Table(
        name = "word_card_note_node_rel",
        indexes = {
                @Index(name = "idx_word_card_note_node_rel_card_id", columnList = "word_card_id"),
                @Index(name = "idx_word_card_note_node_rel_node_id", columnList = "note_node_id")
        }
)
@ToString
public class WordCardNoteNodeRel {

    /**
     * 单词卡片 ID
     */
    @Id
    @Column(name = "word_card_id", nullable = false)
    private Long wordCardId;

    /**
     * 笔记节点 ID
     */
    @Id
    @Column(name = "note_node_id", nullable = false)
    private Long noteNodeId;
}
