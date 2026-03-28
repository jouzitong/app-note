package org.zzt.note.data.core.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.athena.framework.data.jpa.domain.dto.LogicalDeleteDTO;

/**
 *
 * @author zhouzhitong
 * @since 2026/3/27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteNodeDTO extends LogicalDeleteDTO {

    /**
     * 父节点ID（根节点为 null）
     */
    private Long parentId;

    /**
     * 标题
     */
    private String title;

    /**
     * 笔记模板类型（核心字段）
     */
    private String noteType;

    /**
     * 排序
     */
    private Integer sort = 0;

    /**
     * 内容（JSON）
     */
    private String content;

    private NoteNodeMetaDTO meta;

}
