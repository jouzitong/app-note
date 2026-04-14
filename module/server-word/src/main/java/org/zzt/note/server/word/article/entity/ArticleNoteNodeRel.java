package org.zzt.note.server.word.article.entity;

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
 * 文章与笔记节点关联
 *
 * @author zhouzhitong
 * @since 2026/4/7
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@IdClass(ArticleNoteNodeRelId.class)
@Table(
        name = "article_note_node_rel",
        indexes = {
                @Index(name = "idx_article_note_node_rel_article_id", columnList = "article_id"),
                @Index(name = "idx_article_note_node_rel_note_node_id", columnList = "note_node_id")
        }
)
@ToString
public class ArticleNoteNodeRel {

    /**
     * 文章 ID
     */
    @Id
    @Column(name = "article_id", nullable = false)
    private Long articleId;

    /**
     * 笔记节点 ID
     */
    @Id
    @Column(name = "note_node_id", nullable = false)
    private Long noteNodeId;
}
