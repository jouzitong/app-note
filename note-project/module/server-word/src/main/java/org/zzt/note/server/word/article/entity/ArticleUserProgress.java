package org.zzt.note.server.word.article.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.athena.framework.data.jpa.domain.AuditableEntity;

import java.math.BigDecimal;

/**
 * 用户文章阅读进度
 *
 * @author zhouzhitong
 * @since 2026/4/7
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "article_user_progress",
        indexes = {
                @Index(name = "idx_article_user_progress_user_id", columnList = "user_id"),
                @Index(name = "idx_article_user_progress_article_id", columnList = "article_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_article_user_progress_user_article", columnNames = {"user_id", "article_id"})
        }
)
@ToString(callSuper = true, exclude = {"article"})
public class ArticleUserProgress extends AuditableEntity {

    /**
     * 用户 ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 是否收藏
     */
    @Column(name = "favorite", nullable = false)
    private Boolean favorite = false;

    /**
     * 是否完成
     */
    @Column(name = "completed", nullable = false)
    private Boolean completed = false;

    /**
     * 上次阅读段落下标（从 0 开始）
     */
    @Column(name = "last_read_paragraph_index")
    private Integer lastReadParagraphIndex;

    /**
     * 播放速度
     */
    @Column(name = "playback_rate", precision = 4, scale = 2)
    private BigDecimal playbackRate = BigDecimal.valueOf(1.0D);

    /**
     * 文章关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;
}
