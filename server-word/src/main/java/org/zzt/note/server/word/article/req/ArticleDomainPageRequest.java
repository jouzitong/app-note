package org.zzt.note.server.word.article.req;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.athena.framework.data.jdbc.req.BaseRequest;

/**
 * 文章领域分页请求
 *
 * @author zhouzhitong
 * @since 2026/4/7
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleDomainPageRequest extends BaseRequest {

    /**
     * 标题关键字（可空）
     */
    private String keyword;

    /**
     * 用户 ID（可空）
     */
    private Long userId;
}
