package org.zzt.note.server.word.article.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文章与笔记节点关联复合主键
 *
 * @author zhouzhitong
 * @since 2026/4/7
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ArticleNoteNodeRelId implements Serializable {

    private Long articleId;

    private Long noteNodeId;
}
