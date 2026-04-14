package org.zzt.note.server.word.article.repository;

import org.athena.framework.data.jpa.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.zzt.note.server.word.article.entity.Article;

import java.util.Optional;

/**
 * 文章仓储
 *
 * @author zhouzhitong
 * @since 2026/4/7
 */
public interface IArticleRepository extends BaseRepository<Article> {

    Optional<Article> findByArticleCode(String articleCode);

    boolean existsByArticleCode(String articleCode);

    Page<Article> findByTitleContainingOrderByIdDesc(String title, Pageable pageable);
}
