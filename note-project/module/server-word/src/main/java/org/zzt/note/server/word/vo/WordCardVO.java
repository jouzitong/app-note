package org.zzt.note.server.word.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 单词卡详情视图对象（结构参考 refer/node/word-card.json）
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WordCardVO {

    private String id;

    private WordInfo word = new WordInfo();

    private Boolean done = false;

    private ProgressInfo progress = new ProgressInfo();

    private List<TagInfo> tags = new ArrayList<>();

    private Sections sections = new Sections();

    @Deprecated
    private List<ActionInfo> actions = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WordInfo {

        private String text;

        private String level;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressInfo {

        private Boolean done = false;

        private Boolean hard = false;

        private Boolean favorite = false;

        private String status = "NEW";

        private Integer reviewCount = 0;

        private Integer correctCount = 0;

        private Integer wrongCount = 0;

        private LocalDateTime lastReviewedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagInfo {

        private String name;

        private String className;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Sections {

        private MeaningSection meaning = new MeaningSection();

        private ExamplesSection examples = new ExamplesSection();

        private VocabularySection synonyms = new VocabularySection();

        private VocabularySection related = new VocabularySection();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeaningSection {

        private Boolean collapsedByDefault = true;

        private MeaningMeta meta = new MeaningMeta();

        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeaningMeta {

        private String kana;

        private String zh;

        private String romaji;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExamplesSection {

        private Boolean collapsedByDefault = false;

        private List<ExampleItem> items = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExampleItem {

        private String id;

        private String sentence;

        private ExampleExplain explain = new ExampleExplain();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExampleExplain {

        private Boolean collapsedByDefault = true;

        private String reading;

        private String romaji;

        private String meaningZh;

        private List<WordGrammarBreakdownItem> wordGrammarBreakdown = new ArrayList<>();

        private FixedPattern fixedPattern = new FixedPattern();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WordGrammarBreakdownItem {

        private String word;

        private String kana;

        private String desc;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FixedPattern {

        private String pattern;

        private String meaningZh;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VocabularySection {

        private List<VocabularyItem> items = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VocabularyItem {

        private String text;

        private String kana;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionInfo {

        private String key;

        private String icon;

        private String title;
    }
}
