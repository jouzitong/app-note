package org.zzt.note.app.layer.content.req;

import lombok.Data;

/**
 * 聚合内容查询参数。
 */
@Data
public class NoteNodeContentQuery {

    private Integer page;

    private Integer size;

    private Integer index;

    private Long userId;

    private String questionType;

    private Boolean includeChildren;
}
