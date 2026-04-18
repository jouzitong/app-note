package org.zzt.note.data.core.type;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.arthena.framework.common.enums.IEnum;

/**
 * 笔记节点类型。
 * <p>
 * 约定：
 * <br>1. {@code code} 用于后端内部逻辑判断、稳定传输。
 * <br>2. {@code name} 为面向用户的显示名称。
 * <br>3. {@code desc} 为类型说明，便于前后端和运营统一理解。
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
@Getter
public enum NoteType implements IEnum {

    /**
     * 空节点：仅作为占位或结构节点使用，不承载实际内容。
     */
    EMPTY(0, "Empty", "空节点，无内容"),

    /**
     * Markdown 笔记：用于结构化长文、知识整理与文档沉淀。
     */
    MARKDOWN(1, "Markdown", "结构化长文笔记，适合知识沉淀与文档编写"),

    /**
     * 单词卡：用于词汇记忆，通常包含词条、释义、例句等信息。
     */
    WORD_CARD(2, "Word Card", "词汇卡片笔记，适合单词记忆与复习"),

    /**
     * 语句卡：用于句型、表达或语料积累，强调上下文语义。
     */
    SENTENCE(3, "Sentence", "语句/表达卡片，适合例句积累与语感训练"),

    /**
     * 文章卡：用于长文、文档的整理与结构化，通常包含标题、内容、目录等信息。
     */
    ARTICLE(4, "Article", "文章卡片，适合文章、长文、文档的整理与结构化"),

    /**
     * 题目卡：用于题目练习与错题整理，通常包含题干、答案与解析。
     */
    PRACTICE(100, "Practice", "题目练习卡片，适合刷题与错题复盘"),

    ;

    @JsonValue
    private final int code;

    private final String name;
    private final String desc;

    NoteType(int code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }
}
