package org.zzt.note.server.word.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.athena.framework.data.jpa.domain.dto.LogicalDeleteDTO;
import org.zzt.note.data.core.entity.dto.NoteTagDTO;
import org.zzt.note.server.word.entity.meta.WordCardMetaInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 单词卡片 DTO
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WordCardDTO extends LogicalDeleteDTO {

    /**
     * 业务唯一编码（card.id）
     */
    private String cardCode;

    /**
     * 语言环境（locale）
     */
    private String locale;

    /**
     * 单词文本（card.word.text）
     */
    private String wordText;

    /**
     * 卡片元信息（对应 sections 的 meaning/synonyms/related，不包含 examples）
     */
    private WordCardMetaInfo metaInfo = new WordCardMetaInfo();

    /**
     * 标签（复用核心标签表）
     */
    private List<NoteTagDTO> tags = new ArrayList<>();

    /**
     * 例句关联
     */
    private List<ExampleSentenceDTO> examples = new ArrayList<>();
}
