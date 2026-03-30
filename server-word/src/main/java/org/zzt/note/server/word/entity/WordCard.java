package org.zzt.note.server.word.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.athena.framework.data.jpa.domain.LogicalDeleteEntity;
import org.zzt.note.data.core.entity.NoteTag;
import org.zzt.note.server.word.entity.converter.WordCardMetaInfoJsonConverter;
import org.zzt.note.server.word.entity.meta.WordCardMetaInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 单词卡片
 *
 * @author zhouzhitong
 * @since 2026/3/28
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "word_card",
        indexes = {
                @Index(name = "idx_word_card_card_code", columnList = "card_code"),
                @Index(name = "idx_word_card_locale", columnList = "locale"),
                @Index(name = "idx_word_card_word_text", columnList = "word_text")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_word_card_code", columnNames = {"card_code"})
        }
)
@ToString(callSuper = true, exclude = {"tags", "examples"})
public class WordCard extends LogicalDeleteEntity {

    /**
     * 业务唯一编码（card.id）
     */
    @Column(name = "card_code", nullable = false, length = 100)
    private String cardCode;

    /**
     * 语言环境（locale）
     */
    @Column(name = "locale", nullable = false, length = 20)
    private String locale;

    /**
     * 单词文本（card.word.text）
     */
    @Column(name = "word_text", nullable = false, length = 200)
    private String wordText;

    /**
     * 卡片元信息（对应 sections 的 meaning/synonyms/related，不包含 examples）
     */
    @Convert(converter = WordCardMetaInfoJsonConverter.class)
    @Column(name = "meta_info", columnDefinition = "json")
    private WordCardMetaInfo metaInfo = new WordCardMetaInfo();

    /**
     * 标签（复用核心标签表）
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "word_card_tag_rel",
            joinColumns = @JoinColumn(name = "word_card_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"),
            indexes = {
                    @Index(name = "idx_word_card_tag_rel_card_id", columnList = "word_card_id"),
                    @Index(name = "idx_word_card_tag_rel_tag_id", columnList = "tag_id")
            },
            uniqueConstraints = {
                    @UniqueConstraint(name = "uk_word_card_tag_rel", columnNames = {"word_card_id", "tag_id"})
            }
    )
    private List<NoteTag> tags = new ArrayList<>();

    /**
     * 例句关联
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "word_card_example_rel",
            joinColumns = @JoinColumn(name = "word_card_id"),
            inverseJoinColumns = @JoinColumn(name = "example_sentence_id"),
            indexes = {
                    @Index(name = "idx_word_card_example_rel_card_id", columnList = "word_card_id"),
                    @Index(name = "idx_word_card_example_rel_example_id", columnList = "example_sentence_id")
            },
            uniqueConstraints = {
                    @UniqueConstraint(name = "uk_word_card_example_rel", columnNames = {"word_card_id", "example_sentence_id"})
            }
    )
    private List<ExampleSentence> examples = new ArrayList<>();
}
