package org.zzt.note.server.practice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * QuestionNoteNodeRel 复合主键
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionNoteNodeRelId implements Serializable {
    private Long questionId;
    private Long noteNodeId;
}

