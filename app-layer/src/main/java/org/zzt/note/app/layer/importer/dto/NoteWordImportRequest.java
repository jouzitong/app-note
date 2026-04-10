package org.zzt.note.app.layer.importer.dto;

import lombok.Data;
import org.zzt.note.data.core.entity.dto.NoteNodeDTO;
import org.zzt.note.server.word.vo.WordCardVO;

import java.util.List;

@Data
public class NoteWordImportRequest {

    private Meta meta;

    private Payload payload;

    @Data
    public static class Meta {
        private String importId;
        private String source;
        private Options options;
    }

    @Data
    public static class Options {
        private Boolean upsert;
    }

    @Data
    public static class Payload {
        private List<NoteNodeImportItem> noteNodes;
        private List<WordCardVO> wordCards;
    }

    @Data
    public static class NoteNodeImportItem {
        private String nodeKey;
        private String parentNodeKey;
        private NoteNodeDTO noteNode;
        private Object content;
    }
}
