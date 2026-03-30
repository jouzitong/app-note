package org.zzt.note.data.core.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.zzt.note.data.core.entity.dto.NoteNodeDTO;

import java.util.List;

/**
 *
 * @author zhouzhitong
 * @since 2026/3/28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteNodeVO {

    /**
     * 节点信息
     */
    private NoteNodeDTO noteNode;

    /**
     * 表示与注释节点关联的路径列表。每个路径都被封装为一个NoteNodePathVO对象，
     * 其中包含 ID 和标题，分别表示该路径的唯一标识符和描述性标题。
     */
    private List<NoteNodePathVO> paths;

    /**
     * 内容对象
     */
    private Object content;

}
