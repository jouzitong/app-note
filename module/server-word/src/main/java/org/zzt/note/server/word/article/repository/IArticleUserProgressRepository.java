package org.zzt.note.server.word.article.repository;

import org.athena.framework.data.jpa.repository.BaseRepository;
import org.zzt.note.server.word.article.entity.ArticleUserProgress;

import java.util.Optional;

/**
 * 用户文章阅读进度仓储
 *
 * @author zhouzhitong
 * @since 2026/4/7
 */
public interface IArticleUserProgressRepository extends BaseRepository<ArticleUserProgress> {

    Optional<ArticleUserProgress> findByUserIdAndArticleId(Long userId, Long articleId);

    void deleteByArticleId(Long articleId);
}
