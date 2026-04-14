package org.zzt.note.server.word.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.athena.framework.data.jpa.domain.LogicalDeleteEntity;
import org.zzt.note.server.word.entity.converter.UserWordProgressMetaInfoJsonConverter;
import org.zzt.note.server.word.entity.enums.UserWordProgressStatus;
import org.zzt.note.server.word.entity.meta.UserWordProgressMetaInfo;

import java.time.LocalDateTime;

/**
 * 用户单词学习进度
 *
 * @author zhouzhitong
 * @since 2026/3/28
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "user_word_progress",
        indexes = {
                @Index(name = "idx_user_word_progress_user_id", columnList = "user_id"),
                @Index(name = "idx_user_word_progress_status", columnList = "status"),
                @Index(name = "idx_user_word_progress_last_reviewed_at", columnList = "last_reviewed_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_word_progress_user_card", columnNames = {"user_id", "word_card_id"})
        }
)
@ToString(callSuper = true, exclude = "wordCard")
public class UserWordProgress extends LogicalDeleteEntity {

    /**
     * 用户 ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 单词卡
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_card_id", nullable = false)
    private WordCard wordCard;

    /**
     * 学习状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private UserWordProgressStatus status = UserWordProgressStatus.NEW;

    /**
     * 扩展统计信息（JSON）
     */
    @Convert(converter = UserWordProgressMetaInfoJsonConverter.class)
    @Column(name = "meta_info", columnDefinition = "json")
    private UserWordProgressMetaInfo metaInfo = new UserWordProgressMetaInfo();

    /**
     * 最近复习时间
     */
    @Column(name = "last_reviewed_at", columnDefinition = "DATETIME")
    private LocalDateTime lastReviewedAt;
}
