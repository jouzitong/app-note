package org.zzt.note.data.core.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.athena.framework.data.jpa.domain.BaseEntity;

import java.util.ArrayList;
import java.util.List;

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
        name = "note_node_meta",
        indexes = {
                @Index(name = "idx_note_node_meta_node_id", columnList = "node_id")
        }
)
@ToString(callSuper = true, exclude = "node")
public class NoteNodeMeta extends BaseEntity {

    /**
     * 所属笔记节点
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id", nullable = false, unique = true)
    private NoteNode node;

    /**
     * 图标
     */
    @Column(length = 100)
    private String icon;

    /**
     * 标签
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "note_node_meta_tag_rel",
            joinColumns = @JoinColumn(name = "meta_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"),
            indexes = {
                    @Index(name = "idx_meta_tag_rel_meta_id", columnList = "meta_id"),
                    @Index(name = "idx_meta_tag_rel_tag_id", columnList = "tag_id")
            },
            uniqueConstraints = {
                    @UniqueConstraint(name = "uk_meta_tag_rel_meta_tag", columnNames = {"meta_id", "tag_id"})
            }
    )
    private List<NoteTag> tags = new ArrayList<>();

    /**
     * 所属领域（英语 / 日语 / Java 等）
     */
    @Column(length = 50)
    private String subject;

}
