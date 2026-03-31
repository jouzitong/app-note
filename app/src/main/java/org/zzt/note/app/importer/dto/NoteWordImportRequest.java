package org.zzt.note.app.importer.dto;

import lombok.Data;
import org.zzt.note.data.core.entity.dto.NoteNodeDTO;
import org.zzt.note.server.word.vo.WordCardVO;

import java.util.ArrayList;
import java.util.List;

/**
 * 笔记节点 + 单词卡导入请求
 */
@Data
public class NoteWordImportRequest {

    private Meta meta = new Meta();

    private Payload payload = new Payload();

    @Data
    public static class Meta {

        private String version;

        private String source;

        private String importId;

        private String createdAt;

        private Options options = new Options();
    }

    @Data
    public static class Options {

        private Boolean dryRun = Boolean.FALSE;

        private Boolean upsert = Boolean.TRUE;

        private Boolean strict = Boolean.TRUE;
    }

    @Data
    public static class Payload {

        private List<NoteNodeImportItem> noteNodes = new ArrayList<>();

        private List<WordCardVO> wordCards = new ArrayList<>();

        private List<RelationImportItem> relations = new ArrayList<>();
    }

    @Data
    public static class NoteNodeImportItem {

        private String nodeKey;

        private String parentNodeKey;

        private NoteNodeDTO noteNode = new NoteNodeDTO();

        private Object content;
    }

    @Data
    public static class RelationImportItem {

        private String nodeKey;

        private String cardId;

        private Integer order;
    }
}
