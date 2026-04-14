package org.zzt.note.server.word.article.repository;

import org.athena.framework.data.jpa.repository.BaseRepository;
import org.springframework.transaction.annotation.Transactional;
import org.zzt.note.server.word.article.entity.ArticleNoteNodeRel;

import java.util.List;

/**
 * 文章与笔记节点关联仓储
 *
 * @author zhouzhitong
 * @since 2026/4/7
 */
public interface IArticleNoteNodeRelRepository extends BaseRepository<ArticleNoteNodeRel> {

    List<ArticleNoteNodeRel> findByArticleId(Long articleId);

    List<ArticleNoteNodeRel> findByArticleIdIn(List<Long> articleIds);

    List<ArticleNoteNodeRel> findByNoteNodeIdOrderByArticleIdAsc(Long noteNodeId);

    @Transactional
    void deleteByArticleId(Long articleId);
}
