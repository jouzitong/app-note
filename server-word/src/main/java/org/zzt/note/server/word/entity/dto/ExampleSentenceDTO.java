package org.zzt.note.server.word.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.athena.framework.data.jpa.domain.dto.LogicalDeleteDTO;
import org.zzt.note.server.word.entity.meta.ExampleSentenceMetaInfo;

/**
 * 例句 DTO
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExampleSentenceDTO extends LogicalDeleteDTO {

    /**
     * 业务例句编码（如 ex-1）
     */
    private String exampleCode;

    /**
     * 例句原文
     */
    private String sentence;

    /**
     * 扩展信息（读音/释义/语法/固定句型）
     */
    private ExampleSentenceMetaInfo metaInfo = new ExampleSentenceMetaInfo();

    /**
     * 学习权重（数值越小优先级越高）
     */
    private Integer weight = 100;
}
