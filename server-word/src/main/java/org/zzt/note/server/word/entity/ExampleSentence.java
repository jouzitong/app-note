package org.zzt.note.server.word.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.athena.framework.data.jpa.domain.LogicalDeleteEntity;
import org.zzt.note.server.word.entity.converter.ExampleSentenceMetaInfoJsonConverter;
import org.zzt.note.server.word.entity.meta.ExampleSentenceMetaInfo;

/**
 * 例句实体，可复用于不同类型卡片
 *
 * @author zhouzhitong
 * @since 2026/3/28
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "example_sentence",
        indexes = {
                @Index(name = "idx_example_sentence_code", columnList = "example_code")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_example_sentence_code", columnNames = {"example_code"})
        }
)
@ToString(callSuper = true)
public class ExampleSentence extends LogicalDeleteEntity {

    /**
     * 业务例句编码（如 ex-1）
     */
    @Column(name = "example_code", nullable = false, length = 100)
    private String exampleCode;

    /**
     * 例句原文
     */
    @Column(name = "sentence", nullable = false, columnDefinition = "TEXT")
    private String sentence;

    /**
     * 扩展信息（读音/释义/语法/固定句型）
     */
    @Convert(converter = ExampleSentenceMetaInfoJsonConverter.class)
    @Column(name = "meta_info", columnDefinition = "json")
    private ExampleSentenceMetaInfo metaInfo = new ExampleSentenceMetaInfo();

    /**
     * 学习权重（数值越小优先级越高）
     */
    @Column(name = "weight", nullable = false)
    private Integer weight = 100;

}
