package org.zzt.note.server.word.req;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.athena.framework.data.jdbc.req.BaseRequest;

/**
 * 单词卡片领域分页请求
 *
 * @author zhouzhitong
 * @since 2026/4/2
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WordCardDomainPageRequest extends BaseRequest {

    /**
     * 笔记节点ID
     */
    private Long noteId;

    /**
     * 用户ID（可空）
     */
    private Long userId;
}
