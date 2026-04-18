package org.zzt.note.app.layer.importer.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NoteWordImportResult {

    private String importId;

    private Summary summary = new Summary();

    private List<NodeSummary> noteNodes = new ArrayList<>();

    private List<NodeSummary> wordCards = new ArrayList<>();

    private List<RelationSummary> relations = new ArrayList<>();

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
        private int updated;
        private int skipped;
    }

    @Data
    public static class RelationSummary {
        private int total;
        private int created;
        private int reused;
        private int skipped;
    }
}
