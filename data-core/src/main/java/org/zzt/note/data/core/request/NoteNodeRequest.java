package org.zzt.note.data.core.request;

import lombok.Data;
import org.athena.framework.data.jdbc.req.BaseRequest;

/**
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
@Data
public class NoteNodeRequest extends BaseRequest {

    /**
     * 节点ID
     */
    private Long id;

}
