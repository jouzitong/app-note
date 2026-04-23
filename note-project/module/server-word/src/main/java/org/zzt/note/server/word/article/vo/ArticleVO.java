package org.zzt.note.server.word.article.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 文章阅读视图对象（结构参考 refer/node/文章/article-data.json）
 *
 * @author zhouzhitong
 * @since 2026/4/7
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleVO {

    private String id;

    private Long noteNodeId;

    private String title;

    private List<List<TokenInfo>> paragraphs = new ArrayList<>();

    private List<String> translation = new ArrayList<>();

    private Progress progress = new Progress();

    private Knowledge knowledge = new Knowledge();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenInfo {

        private String text;

        private String kana;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Progress {

        private Boolean favorite = false;

        private Boolean completed = false;

        private Integer lastReadParagraphIndex = 0;

        private Double playbackRate = 1.0D;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Knowledge {

        private List<CoreVocabulary> coreVocabulary = new ArrayList<>();

        private List<CoreSentencePattern> coreSentencePatterns = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoreVocabulary {

        private String jp;

        private String kana;

        private String meaning;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoreSentencePattern {

        private String jp;

        private String meaning;
    }
}
