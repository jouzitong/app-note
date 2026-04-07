package org.zzt.note.server.word.article.entity.meta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 文章内容元信息（JSON）
 *
 * @author zhouzhitong
 * @since 2026/4/7
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleMetaInfo {

    /**
     * 段落列表（每段由若干 token 构成）
     */
    private List<List<TokenInfo>> paragraphs = new ArrayList<>();

    /**
     * 段落翻译（按段落下标对应）
     */
    private List<String> translation = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenInfo {

        private String text;

        private String kana;
    }
}
