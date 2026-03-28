package org.zzt.note.data.core.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.athena.framework.data.jpa.domain.dto.BaseDTO;
import org.zzt.note.data.core.entity.NoteNode;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author zhouzhitong
 * @since 2026/3/27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteNodeMetaDTO extends BaseDTO {

    /**
     * 所属笔记节点
     */
    private NoteNode node;

    /**
     * 图标
     */
    private String icon;

    /**
     * 标签
     */
    private List<NoteTagDTO> tags = new ArrayList<>();

    /**
     * 所属领域（英语 / 日语 / Java 等）
     */
    private String subject;

}
