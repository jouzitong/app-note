package org.zzt.note.data.core.type;

import lombok.Getter;
import org.arthena.framework.common.enums.IEnum;

/**
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
@Getter
public enum NoteType implements IEnum {

    // markdown、word-card、sentence、questions
    


    ;


    private final int code;

    private final String name;
    private final String desc;

    NoteType(int code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }
}
