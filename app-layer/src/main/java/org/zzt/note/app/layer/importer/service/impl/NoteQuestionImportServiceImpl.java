package org.zzt.note.app.layer.importer.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.zzt.note.app.layer.importer.dto.NoteQuestionImportRequest;
import org.zzt.note.app.layer.importer.dto.NoteQuestionImportResult;
import org.zzt.note.app.layer.importer.service.INoteQuestionImportService;
import org.zzt.note.data.core.dto.NoteNodeAddDTO;
import org.zzt.note.data.core.entity.NoteNode;
import org.zzt.note.data.core.entity.dto.NoteNodeDTO;
import org.zzt.note.data.core.repository.INoteNodeRepository;
import org.zzt.note.data.core.service.INoteNodeDomainService;
import org.zzt.note.server.practice.entity.Question;
import org.zzt.note.server.practice.entity.QuestionNoteNodeRel;
import org.zzt.note.server.practice.repository.IQuestionNoteNodeRelRepository;
import org.zzt.note.server.practice.repository.IQuestionRepository;
import org.zzt.note.server.practice.service.IQuestionDomainService;
import org.zzt.note.server.practice.vo.QuestionVO;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 笔记节点 + 题库导入服务。
 * <p>
 * 规则与 NoteWordImportServiceImpl 保持一致：
 * - noteNodes 按 nodeKey/parentNodeKey 创建（支持链式父子）
 * - questions 以 questionCode 幂等
 * - 默认将所有题目挂载到 noteNodes 的最后一个节点（通常是 NoteType=QUESTIONS）
 */
@Service
@AllArgsConstructor
public class NoteQuestionImportServiceImpl implements INoteQuestionImportService {

    private final INoteNodeRepository noteNodeRepository;

    private final INoteNodeDomainService noteNodeDomainService;

    private final IQuestionRepository questionRepository;

    private final IQuestionDomainService questionDomainService;

    private final IQuestionNoteNodeRelRepository questionNoteNodeRelRepository;

    @Override
    @Transactional
    public NoteQuestionImportResult importData(NoteQuestionImportRequest request) {
        NoteQuestionImportRequest safeRequest = request == null ? new NoteQuestionImportRequest() : request;
        NoteQuestionImportRequest.Payload payload = safeRequest.getPayload() == null
                ? new NoteQuestionImportRequest.Payload()
                : safeRequest.getPayload();

        List<NoteQuestionImportRequest.NoteNodeImportItem> noteNodes = payload.getNoteNodes() == null
                ? List.of()
                : payload.getNoteNodes();
        List<QuestionVO> questions = payload.getQuestions() == null ? List.of() : payload.getQuestions();

        boolean upsertEnabled = safeBoolean(safeRequest.getMeta() == null ? null : safeRequest.getMeta().getOptions());

        NoteQuestionImportResult result = new NoteQuestionImportResult();
        result.setImportId(safeRequest.getMeta() == null ? null : safeRequest.getMeta().getImportId());
        result.getSummary().getNoteNodes().setTotal(noteNodes.size());
        result.getSummary().getQuestions().setTotal(questions.size());
        result.getSummary().getRelations().setTotal(questions.size());

        Map<String, Long> nodeKeyToId = importNoteNodes(noteNodes, result);
        Map<String, Long> questionCodeToId = importQuestions(questions, result, upsertEnabled);
        importRelationsToLastNoteNode(noteNodes, nodeKeyToId, questionCodeToId, result);
        return result;
    }

    private boolean safeBoolean(NoteQuestionImportRequest.Options options) {
        if (options == null || options.getUpsert() == null) {
            return false;
        }
        return Boolean.TRUE.equals(options.getUpsert());
    }

    private Map<String, Long> importNoteNodes(List<NoteQuestionImportRequest.NoteNodeImportItem> noteNodes,
                                              NoteQuestionImportResult result) {
        Map<String, Long> nodeKeyToId = new HashMap<>();
        if (CollectionUtils.isEmpty(noteNodes)) {
            return nodeKeyToId;
        }

        Set<String> unresolved = new HashSet<>();
        for (NoteQuestionImportRequest.NoteNodeImportItem item : noteNodes) {
            String nodeKey = normalizeKey(item == null ? null : item.getNodeKey());
            if (nodeKey == null) {
                continue;
            }
            unresolved.add(nodeKey);
        }

        while (!unresolved.isEmpty()) {
            boolean progressed = false;
            for (NoteQuestionImportRequest.NoteNodeImportItem item : noteNodes) {
                String nodeKey = normalizeKey(item == null ? null : item.getNodeKey());
                if (nodeKey == null || !unresolved.contains(nodeKey)) {
                    continue;
                }

                Optional<NoteNode> existing = noteNodeRepository.findByNoteKey(nodeKey);
                if (existing.isPresent()) {
                    nodeKeyToId.put(nodeKey, existing.get().getId());
                    unresolved.remove(nodeKey);
                    result.getSummary().getNoteNodes().setReused(result.getSummary().getNoteNodes().getReused() + 1);
                    progressed = true;
                    continue;
                }

                Long parentId = null;
                String parentNodeKey = normalizeKey(item.getParentNodeKey());
                if (parentNodeKey != null) {
                    parentId = nodeKeyToId.get(parentNodeKey);
                    if (parentId == null) {
                        Optional<NoteNode> existingParent = noteNodeRepository.findByNoteKey(parentNodeKey);
                        if (existingParent.isPresent()) {
                            parentId = existingParent.get().getId();
                            nodeKeyToId.put(parentNodeKey, parentId);
                        } else {
                            continue;
                        }
                    }
                }

                createNoteNode(nodeKey, parentId, item);
                Long createdId = noteNodeRepository.findByNoteKey(nodeKey)
                        .map(NoteNode::getId)
                        .orElseThrow(() -> new IllegalStateException("Created noteNode not found by noteKey: " + nodeKey));

                nodeKeyToId.put(nodeKey, createdId);
                unresolved.remove(nodeKey);
                result.getSummary().getNoteNodes().setCreated(result.getSummary().getNoteNodes().getCreated() + 1);
                progressed = true;
            }

            if (!progressed) {
                throw new IllegalArgumentException("Cannot resolve noteNodes parentNodeKey chain, unresolved keys: " + unresolved);
            }
        }

        return nodeKeyToId;
    }

    private void createNoteNode(String nodeKey, Long parentId, NoteQuestionImportRequest.NoteNodeImportItem item) {
        NoteNodeDTO source = item.getNoteNode() == null ? new NoteNodeDTO() : item.getNoteNode();
        NoteNodeDTO noteNodeDTO = new NoteNodeDTO();
        noteNodeDTO.setNoteKey(nodeKey);
        noteNodeDTO.setParentId(parentId);
        noteNodeDTO.setTitle(source.getTitle());
        noteNodeDTO.setNoteType(source.getNoteType());
        noteNodeDTO.setSort(source.getSort() == null ? 0 : source.getSort());
        noteNodeDTO.setMeta(source.getMeta());

        NoteNodeAddDTO addDTO = new NoteNodeAddDTO();
        addDTO.setNoteNode(noteNodeDTO);
        addDTO.setContent(item.getContent());
        noteNodeDomainService.add(addDTO);
    }

    private Map<String, Long> importQuestions(List<QuestionVO> questions,
                                              NoteQuestionImportResult result,
                                              boolean upsertEnabled) {
        Map<String, Long> codeToId = new HashMap<>();
        for (QuestionVO questionVO : questions) {
            String code = normalizeKey(questionVO == null ? null : questionVO.getQuestionCode());
            if (code == null) {
                result.getSummary().getQuestions().setSkipped(result.getSummary().getQuestions().getSkipped() + 1);
                continue;
            }

            Optional<Question> existing = questionRepository.findByQuestionCode(code);
            if (existing.isPresent() && !upsertEnabled) {
                codeToId.put(code, existing.get().getId());
                result.getSummary().getQuestions().setReused(result.getSummary().getQuestions().getReused() + 1);
                continue;
            }

            QuestionVO input = questionVO == null ? new QuestionVO() : questionVO;
            input.setQuestionCode(code);
            QuestionVO saved = questionDomainService.upsert(input);
            if (saved == null || saved.getId() == null) {
                throw new IllegalStateException("Upsert question failed, code=" + code);
            }
            codeToId.put(code, saved.getId());
            if (existing.isPresent()) {
                result.getSummary().getQuestions().setUpdated(result.getSummary().getQuestions().getUpdated() + 1);
            } else {
                result.getSummary().getQuestions().setCreated(result.getSummary().getQuestions().getCreated() + 1);
            }
        }
        return codeToId;
    }

    private void importRelationsToLastNoteNode(List<NoteQuestionImportRequest.NoteNodeImportItem> noteNodes,
                                               Map<String, Long> nodeKeyToId,
                                               Map<String, Long> questionCodeToId,
                                               NoteQuestionImportResult result) {
        if (CollectionUtils.isEmpty(questionCodeToId)) {
            return;
        }

        if (CollectionUtils.isEmpty(noteNodes)) {
            throw new IllegalArgumentException("noteNodes cannot be empty when questions exist");
        }

        NoteQuestionImportRequest.NoteNodeImportItem lastNode = noteNodes.get(noteNodes.size() - 1);
        String lastNodeKey = normalizeKey(lastNode == null ? null : lastNode.getNodeKey());
        if (lastNodeKey == null) {
            throw new IllegalArgumentException("Last noteNodes.nodeKey cannot be blank");
        }
        Long noteNodeId = nodeKeyToId.get(lastNodeKey);
        if (noteNodeId == null) {
            throw new IllegalArgumentException("Last noteNode not found in import context: " + lastNodeKey);
        }

        for (Map.Entry<String, Long> entry : questionCodeToId.entrySet()) {
            String questionCode = entry.getKey();
            Long questionId = entry.getValue();
            if (questionId == null) {
                throw new IllegalArgumentException("Question not found in import context: " + questionCode);
            }

            boolean exists = questionNoteNodeRelRepository.findByQuestionId(questionId)
                    .stream()
                    .anyMatch(item -> item.getNoteNodeId().equals(noteNodeId));
            if (exists) {
                result.getSummary().getRelations().setReused(result.getSummary().getRelations().getReused() + 1);
                continue;
            }

            QuestionNoteNodeRel rel = new QuestionNoteNodeRel();
            rel.setQuestionId(questionId);
            rel.setNoteNodeId(noteNodeId);
            questionNoteNodeRelRepository.save(rel);
            result.getSummary().getRelations().setCreated(result.getSummary().getRelations().getCreated() + 1);
        }
    }

    private String normalizeKey(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

