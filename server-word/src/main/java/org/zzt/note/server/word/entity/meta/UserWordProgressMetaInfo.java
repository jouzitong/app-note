package org.zzt.note.server.word.entity.meta;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 用户单词学习进度扩展信息
 *
 * @author zhouzhitong
 * @since 2026/3/28
 */
@Getter
@Setter
@NoArgsConstructor
public class UserWordProgressMetaInfo {

    /**
     * 是否易错
     */
    private Boolean hard = false;

    /**
     * 是否收藏
     */
    private Boolean favorite = false;

    /**
     * 复习次数
     */
    private Integer reviewCount = 0;

    /**
     * 答对次数
     */
    private Integer correctCount = 0;

    /**
     * 答错次数
     */
    private Integer wrongCount = 0;
}
