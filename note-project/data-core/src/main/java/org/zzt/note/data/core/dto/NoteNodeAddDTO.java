package org.zzt.note.data.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.zzt.note.data.core.entity.dto.NoteNodeDTO;

import java.util.List;

/**
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteNodeAddDTO {

    private NoteNodeDTO noteNode;

    private List<Long> pathIds;

    /**
     * 节点内容（JSON 或对象）
     */
    private Object content;

}
