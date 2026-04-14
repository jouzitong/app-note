package org.zzt.note.server.word.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 单词卡片与笔记节点关联表复合主键
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WordCardNoteNodeRelId implements Serializable {

    private Long wordCardId;

    private Long noteNodeId;
}
