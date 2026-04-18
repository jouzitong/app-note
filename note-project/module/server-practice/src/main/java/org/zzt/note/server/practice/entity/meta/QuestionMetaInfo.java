package org.zzt.note.server.practice.entity.meta;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 题目结构载荷（JSON）
 * <p>
 * 设计目标：结构可扩展，避免拆表；不依赖 options 级检索。
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
@Getter
@Setter
public class QuestionMetaInfo {

    private Display display = new Display();

    /**
     * 选择题选项（可选）
     */
    private List<Option> options = new ArrayList<>();

    /**
     * 填空题空位定义（可选）
     */
    private List<Blank> blanks = new ArrayList<>();

    /**
     * 标准答案（仅用于判题与提交结果回包）
     */
    private Map<String, Object> answerKey = new LinkedHashMap<>();

    /**
     * 解析（可选）
     */
    private Map<String, Object> analysis = new LinkedHashMap<>();

    /**
     * 素材（可选：音频/图片/视频等）
     */
    private List<Asset> assets = new ArrayList<>();

    /**
     * 扩展字段（标签/来源/知识点等）
     */
    private Map<String, Object> ext = new LinkedHashMap<>();

    @Getter
    @Setter
    public static class Display {
        private String typeLabel;
        private String section;
        private Integer score;
    }

    @Getter
    @Setter
    public static class Option {
        private String key;
        private Map<String, Object> content = new LinkedHashMap<>();
        private Integer sort = 0;
    }

    @Getter
    @Setter
    public static class Blank {
        private String key;
        private String hint;
    }

    @Getter
    @Setter
    public static class Asset {
        private String type;
        private String url;
        private Map<String, Object> meta = new LinkedHashMap<>();
    }
}

