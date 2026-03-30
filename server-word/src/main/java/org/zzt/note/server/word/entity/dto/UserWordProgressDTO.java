package org.zzt.note.server.word.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.athena.framework.data.jpa.domain.dto.LogicalDeleteDTO;
import org.zzt.note.server.word.entity.meta.UserWordProgressMetaInfo;

import java.time.LocalDateTime;

/**
 * 用户单词学习进度 DTO
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserWordProgressDTO extends LogicalDeleteDTO {

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 单词卡 ID
     */
    private Long wordCardId;

    /**
     * 学习状态
     */
    private String status;

    /**
     * 扩展统计信息（JSON）
     */
    private UserWordProgressMetaInfo metaInfo = new UserWordProgressMetaInfo();

    /**
     * 最近复习时间
     */
    private LocalDateTime lastReviewedAt;
}
