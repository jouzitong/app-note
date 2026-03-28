package org.zzt.note.data.core.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.athena.framework.data.jpa.domain.LogicalDeleteEntity;

/**
 *
 * @author zhouzhitong
 * @since 2026/3/26
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "note_node",
        indexes = {
                @Index(name = "idx_note_node_parent_id", columnList = "parent_id"),
                @Index(name = "idx_note_node_note_type", columnList = "note_type"),
                @Index(name = "idx_note_node_parent_sort", columnList = "parent_id,sort")
        }
)
@ToString(callSuper = true, exclude = "meta")
public class NoteNode extends LogicalDeleteEntity {

    /**
     * 父节点ID（根节点为 null）
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 标题
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 笔记模板类型（核心字段）
     */
    @Column(name = "note_type", length = 50)
    private String noteType;

    /**
     * 排序
     */
    @Column(nullable = false)
    private Integer sort = 0;

    /**
     * 内容（JSON）
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 节点附加信息（subject/icon/tags）
     */
    @OneToOne(mappedBy = "node", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private NoteNodeMeta meta;

    public void setMeta(NoteNodeMeta meta) {
        if (this.meta != null && this.meta != meta) {
            this.meta.setNode(null);
        }
        this.meta = meta;
        if (meta != null && meta.getNode() != this) {
            meta.setNode(this);
        }
    }

}
