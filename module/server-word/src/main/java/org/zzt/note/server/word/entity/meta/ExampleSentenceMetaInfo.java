package org.zzt.note.server.word.entity.meta;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 例句扩展元信息
 *
 * @author zhouzhitong
 * @since 2026/3/28
 */
@Getter
@Setter
@NoArgsConstructor
public class ExampleSentenceMetaInfo {

    /**
     * 假名读音
     */
    private String reading;

    /**
     * 罗马音
     */
    private String romaji;

    /**
     * 中文释义
     */
    private String meaningZh;

    /**
     * 词法/语法拆解
     */
    private List<WordGrammarBreakdownItem> wordGrammarBreakdown = new ArrayList<>();

    /**
     * 固定句型
     */
    private FixedPattern fixedPattern = new FixedPattern();

    @Getter
    @Setter
    @NoArgsConstructor
    public static class FixedPattern {

        private String pattern;

        private String meaningZh;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class WordGrammarBreakdownItem {

        private String word;

        private String kana;

        private String desc;
    }
}
