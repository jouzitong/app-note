package org.zzt.note.app.layer.content.vo;

import lombok.Data;
import org.zzt.note.data.core.entity.dto.NoteNodeDTO;
import org.zzt.note.data.core.vo.NoteNodePathVO;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 节点聚合内容返回对象。
 */
@Data
public class NoteNodeContentAppVO {

    private NoteNodeDTO noteNode;

    private List<NoteNodePathVO> paths;

    private List<NoteNodePathVO> childNoteNodes;

    private String noteType;

    private String contentType;

    private Object content;

    private Map<String, Object> ext = new LinkedHashMap<>();
}
