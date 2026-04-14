package org.zzt.note.server.word.article.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.athena.framework.data.jpa.domain.AuditableEntity;
import org.zzt.note.server.word.article.entity.converter.ArticleMetaInfoJsonConverter;
import org.zzt.note.server.word.article.entity.meta.ArticleMetaInfo;

/**
 * 阅读文章
 *
 * @author zhouzhitong
 * @since 2026/4/7
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "article",
        indexes = {
                @Index(name = "idx_article_article_code", columnList = "article_code"),
                @Index(name = "idx_article_title", columnList = "title")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_article_article_code", columnNames = {"article_code"})
        }
)
@ToString(callSuper = true)
public class Article extends AuditableEntity {

    /**
     * 业务唯一编码（JSON id）
     */
    @Column(name = "article_code", nullable = false, length = 100)
    private String articleCode;

    /**
     * 标题
     */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /**
     * 文章元信息（段落 + 翻译）
     */
    @Convert(converter = ArticleMetaInfoJsonConverter.class)
    @Column(name = "meta_info", columnDefinition = "json")
    private ArticleMetaInfo metaInfo = new ArticleMetaInfo();
}
