package org.zzt.note.server.word.entity.meta;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 单词卡片元信息（sections 中除 examples 外的内容）
 *
 * @author zhouzhitong
 * @since 2026/3/28
 */
@Getter
@Setter
@NoArgsConstructor
public class WordCardMetaInfo {

    /**
     * 含义模块
     */
    private MeaningSection meaning = new MeaningSection();

    /**
     * 同义词模块
     */
    private VocabularySection synonyms = new VocabularySection();

    /**
     * 关联词模块
     */
    private VocabularySection related = new VocabularySection();

    @Getter
    @Setter
    @NoArgsConstructor
    public static class MeaningSection {

        private Boolean collapsedByDefault = true;

        private MeaningMeta meta = new MeaningMeta();

        private String description;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class MeaningMeta {

        private String kana;

        private String zh;

        private String romaji;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class VocabularySection {

        private List<VocabularyItem> items = new ArrayList<>();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class VocabularyItem {

        private String text;

        private String kana;
    }
}

