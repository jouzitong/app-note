package org.zzt.note.server.word.entity.enums;

import lombok.Getter;
import org.arthena.framework.common.enums.IEnum;

/**
 * 单词学习进度状态
 *
 * @author zhouzhitong
 * @since 2026/3/28
 */
@Getter
public enum UserWordProgressStatus implements IEnum {

    /**
     * 新建
     */
    NEW(0, "新建"),

    /**
     * 学习中
     */
    LEARNING(1, "学习中"),

    /**
     * 已掌握
     */
    MASTERED(2,"已掌握");

    private final int code;
    private final String name;

    UserWordProgressStatus(int code, String name) {
        this.code = code;
        this.name = name;
    }
}
