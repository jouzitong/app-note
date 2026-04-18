package org.zzt.note.app.layer.ops.dto;

import lombok.Data;

@Data
public class NoteNodeOpsDeleteResult {

    private Long rootNoteNodeId;

    private int targetedNoteNodes;

    private int deletedWordCards;

    private int keptWordCardsShared;

    private int deletedExamples;

    private int keptExamplesShared;
}
