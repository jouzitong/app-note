package org.zzt.note.server.word.article.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 文章阅读聚合视图对象（按 noteNode 返回多篇文章）
 *
 * @author zhouzhitong
 * @since 2026/4/23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleReaderVO {

    private Long noteNodeId;

    private Integer currentArticleIndex = 0;

    private List<ArticleVO> articles = new ArrayList<>();
}
