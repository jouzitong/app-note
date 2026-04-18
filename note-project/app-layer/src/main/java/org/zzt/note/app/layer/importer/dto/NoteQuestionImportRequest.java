package org.zzt.note.app.layer.importer.dto;

import lombok.Data;
import org.zzt.note.data.core.entity.dto.NoteNodeDTO;
import org.zzt.note.server.practice.vo.QuestionVO;

import java.util.List;

@Data
public class NoteQuestionImportRequest {

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
        /**
         * 是否允许更新已存在的题目（基于 questionCode）。
         */
        private Boolean upsert;
    }

    @Data
    public static class Payload {
        private List<NoteNodeImportItem> noteNodes;
        private List<QuestionVO> questions;
    }

    @Data
    public static class NoteNodeImportItem {
        private String nodeKey;
        private String parentNodeKey;
        private NoteNodeDTO noteNode;
        private Object content;
    }
}

