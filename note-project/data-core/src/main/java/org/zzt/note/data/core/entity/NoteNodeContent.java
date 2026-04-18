package org.zzt.note.data.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.athena.framework.data.jpa.domain.BaseEntity;

/**
 * 笔记节点内容（独立内容表）
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "note_node_content",
        indexes = {
                @Index(name = "idx_note_node_content_node_id", columnList = "node_id")
        }
)
@ToString(callSuper = true, exclude = "node")
public class NoteNodeContent extends BaseEntity {

    /**
     * 所属笔记节点
     */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "node_id", nullable = false, unique = true)
    private NoteNode node;

    /**
     * 内容（JSON）
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    public void setNode(NoteNode node) {
        if (this.node != null && this.node != node) {
            this.node.setNodeContent(null);
        }
        this.node = node;
        if (node != null && node.getNodeContent() != this) {
            node.setNodeContent(this);
        }
    }
}
