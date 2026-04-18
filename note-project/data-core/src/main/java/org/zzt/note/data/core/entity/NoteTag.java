package org.zzt.note.data.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.athena.framework.data.jpa.domain.BaseEntity;

/**
 *
 * @author zhouzhitong
 * @since 2026/3/27
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "note_tag",
        indexes = {
                @Index(name = "idx_note_tag_biz_type", columnList = "biz_type"),
                @Index(name = "idx_note_tag_label", columnList = "label")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_note_tag_biz_label", columnNames = {"biz_type", "label"})
        }
)
//@ToString(callSuper = true, exclude = "metas")
public class NoteTag extends BaseEntity {

//    /**
//     * 被哪些节点元数据引用
//     */
//    @ManyToMany(mappedBy = "tags")
//    private List<NoteNodeMeta> metas = new ArrayList<>();

    /**
     * 业务类型
     */
    @Column(name = "biz_type", nullable = false, length = 50)
    private String bizType;

    /**
     * 标签名称
     */
    @Column(nullable = false, length = 100)
    private String label;

    /**
     * 样式类名
     */
    @Column(name = "class_name", length = 100)
    private String className;
}
