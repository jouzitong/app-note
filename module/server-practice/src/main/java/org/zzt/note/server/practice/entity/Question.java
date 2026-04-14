package org.zzt.note.server.practice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.athena.framework.data.jpa.domain.AuditableEntity;
import org.zzt.note.server.practice.entity.converter.QuestionMetaInfoJsonConverter;
import org.zzt.note.server.practice.entity.enums.QuestionType;
import org.zzt.note.server.practice.entity.meta.QuestionMetaInfo;

/**
 * 题目
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "question",
        indexes = {
                @Index(name = "idx_question_question_code", columnList = "question_code"),
                @Index(name = "idx_question_title", columnList = "title"),
                @Index(name = "idx_question_type", columnList = "question_type"),
                @Index(name = "idx_question_locale", columnList = "locale")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_question_question_code", columnNames = {"question_code"})
        }
)
@ToString(callSuper = true)
public class Question extends AuditableEntity {

    @Column(name = "question_code", nullable = false, length = 100)
    private String questionCode;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "stem", nullable = false, columnDefinition = "TEXT")
    private String stem;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 30)
    private QuestionType questionType;

    @Column(name = "difficulty")
    private Integer difficulty;

    @Column(name = "locale", length = 20)
    private String locale;

    @Convert(converter = QuestionMetaInfoJsonConverter.class)
    @Column(name = "meta_info", columnDefinition = "json")
    private QuestionMetaInfo metaInfo = new QuestionMetaInfo();
}

