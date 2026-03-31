package org.zzt.note.app.ops.dto;

import lombok.Data;

/**
 * 节点运维删除结果
 */
@Data
public class NoteNodeOpsDeleteResult {

    private Long rootNoteNodeId;

    private int targetedNoteNodes;

    private int deletedWordCards;

    private int keptWordCardsShared;

    private int deletedExamples;

    private int keptExamplesShared;
}
