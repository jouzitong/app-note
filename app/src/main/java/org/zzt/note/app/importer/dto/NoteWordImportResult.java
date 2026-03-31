package org.zzt.note.app.importer.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 导入结果
 */
@Data
public class NoteWordImportResult {

    private String importId;

    private Summary summary = new Summary();

    private List<String> warnings = new ArrayList<>();

    @Data
    public static class Summary {

        private NodeSummary noteNodes = new NodeSummary();

        private NodeSummary wordCards = new NodeSummary();

        private RelationSummary relations = new RelationSummary();
    }

    @Data
    public static class NodeSummary {

        private int total;

        private int created;

        private int reused;
    }

    @Data
    public static class RelationSummary {

        private int total;

        private int created;

        private int reused;
    }
}
