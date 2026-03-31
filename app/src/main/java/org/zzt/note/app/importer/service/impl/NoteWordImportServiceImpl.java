package org.zzt.note.app.importer.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.zzt.note.app.importer.dto.NoteWordImportRequest;
import org.zzt.note.app.importer.dto.NoteWordImportResult;
import org.zzt.note.app.importer.service.INoteWordImportService;
import org.zzt.note.data.core.dto.NoteNodeAddDTO;
import org.zzt.note.data.core.entity.NoteNode;
import org.zzt.note.data.core.entity.NoteTag;
import org.zzt.note.data.core.entity.dto.NoteNodeDTO;
import org.zzt.note.data.core.entity.dto.NoteNodeMetaDTO;
import org.zzt.note.data.core.entity.dto.NoteTagDTO;
import org.zzt.note.data.core.repository.INoteNodeRepository;
import org.zzt.note.data.core.repository.INoteTagRepository;
import org.zzt.note.data.core.service.INoteNodeDomainService;
import org.zzt.note.server.word.entity.WordCard;
import org.zzt.note.server.word.entity.WordCardNoteNodeRel;
import org.zzt.note.server.word.entity.ExampleSentence;
import org.zzt.note.server.word.entity.meta.ExampleSentenceMetaInfo;
import org.zzt.note.server.word.repository.IExampleSentenceRepository;
import org.zzt.note.server.word.repository.IWordCardNoteNodeRelRepository;
import org.zzt.note.server.word.repository.IWordCardRepository;
import org.zzt.note.server.word.service.IWordCardDomainService;
import org.zzt.note.server.word.vo.WordCardVO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 笔记节点 + 单词卡导入服务
 */
@Service
@AllArgsConstructor
public class NoteWordImportServiceImpl implements INoteWordImportService {

    private static final String NOTE_TYPE_EMPTY = "EMPTY";

    private static final String NOTE_TYPE_WORD_CARD = "WORD_CARD";

    private final INoteNodeRepository noteNodeRepository;

    private final INoteNodeDomainService noteNodeDomainService;

    private final INoteTagRepository noteTagRepository;

    private final IWordCardRepository wordCardRepository;

    private final IExampleSentenceRepository exampleSentenceRepository;

    private final IWordCardDomainService wordCardDomainService;

    private final IWordCardNoteNodeRelRepository wordCardNoteNodeRelRepository;

    @Override
    @Transactional
    public NoteWordImportResult importData(NoteWordImportRequest request) {
        NoteWordImportRequest safeRequest = request == null ? new NoteWordImportRequest() : request;
        NoteWordImportRequest.Payload payload = safeRequest.getPayload() == null
                ? new NoteWordImportRequest.Payload()
                : safeRequest.getPayload();

        List<NoteWordImportRequest.NoteNodeImportItem> noteNodes = payload.getNoteNodes() == null
                ? List.of()
                : payload.getNoteNodes();
        List<WordCardVO> wordCards = payload.getWordCards() == null
                ? List.of()
                : payload.getWordCards();

        validateRequest(noteNodes, wordCards);

        NoteWordImportResult result = new NoteWordImportResult();
        result.setImportId(safeRequest.getMeta() == null ? null : safeRequest.getMeta().getImportId());
        result.getSummary().getNoteNodes().setTotal(noteNodes.size());
        result.getSummary().getWordCards().setTotal(wordCards.size());
        result.getSummary().getRelations().setTotal(wordCards.size());

        Map<String, Long> nodeKeyToId = importNoteNodes(noteNodes, result);
        Map<String, Long> cardIdToId = importWordCards(wordCards, result);
        importRelationsToLastNoteNode(noteNodes, nodeKeyToId, cardIdToId, result);

        return result;
    }

    private void validateRequest(List<NoteWordImportRequest.NoteNodeImportItem> noteNodes,
                                 List<WordCardVO> wordCards) {
        Set<String> nodeKeys = new HashSet<>();
        int lastNodeIndex = noteNodes.size() - 1;
        for (int i = 0; i < noteNodes.size(); i++) {
            NoteWordImportRequest.NoteNodeImportItem noteNodeItem = noteNodes.get(i);
            String nodeKey = normalizeKey(noteNodeItem == null ? null : noteNodeItem.getNodeKey());
            if (nodeKey == null) {
                throw new IllegalArgumentException("noteNodes.nodeKey cannot be blank");
            }
            if (!nodeKeys.add(nodeKey)) {
                throw new IllegalArgumentException("Duplicate nodeKey in payload: " + nodeKey);
            }

            String noteType = normalizeKey(noteNodeItem == null || noteNodeItem.getNoteNode() == null
                    ? null
                    : noteNodeItem.getNoteNode().getNoteType());
            if (noteType != null && !NOTE_TYPE_EMPTY.equals(noteType) && !NOTE_TYPE_WORD_CARD.equals(noteType)) {
                throw new IllegalArgumentException("noteNodes.noteType only supports EMPTY or WORD_CARD, nodeKey: " + nodeKey);
            }
            if (NOTE_TYPE_WORD_CARD.equals(noteType) && i != lastNodeIndex) {
                throw new IllegalArgumentException("Only the last noteNode can use WORD_CARD, nodeKey: " + nodeKey);
            }
        }

        Set<String> cardIds = new HashSet<>();
        for (WordCardVO wordCard : wordCards) {
            String cardId = normalizeKey(wordCard == null ? null : wordCard.getId());
            if (cardId == null) {
                throw new IllegalArgumentException("wordCards.id cannot be blank");
            }
            if (!cardIds.add(cardId)) {
                throw new IllegalArgumentException("Duplicate wordCards.id in payload: " + cardId);
            }
            validateWordCardExamples(wordCard);
        }
        if (!CollectionUtils.isEmpty(wordCards) && CollectionUtils.isEmpty(noteNodes)) {
            throw new IllegalArgumentException("noteNodes cannot be empty when wordCards exist");
        }
        if (!CollectionUtils.isEmpty(wordCards)) {
            NoteWordImportRequest.NoteNodeImportItem lastNode = noteNodes.get(lastNodeIndex);
            String lastType = normalizeKey(lastNode == null || lastNode.getNoteNode() == null
                    ? null
                    : lastNode.getNoteNode().getNoteType());
            if (!NOTE_TYPE_WORD_CARD.equals(lastType)) {
                throw new IllegalArgumentException("The last noteNode.noteType must be WORD_CARD when wordCards exist");
            }
        }
    }

    private void validateWordCardExamples(WordCardVO wordCard) {
        List<WordCardVO.ExampleItem> items = wordCard == null
                || wordCard.getSections() == null
                || wordCard.getSections().getExamples() == null
                ? null
                : wordCard.getSections().getExamples().getItems();

        String cardId = normalizeKey(wordCard == null ? null : wordCard.getId());
        if (CollectionUtils.isEmpty(items)) {
            throw new IllegalArgumentException("wordCards.examples.items cannot be empty, cardId: " + cardId);
        }

        for (WordCardVO.ExampleItem item : items) {
            String exampleId = normalizeKey(item == null ? null : item.getId());
            String sentence = normalizeKey(item == null ? null : item.getSentence());
            if (exampleId == null || sentence == null) {
                throw new IllegalArgumentException("wordCards.examples item id/sentence cannot be blank, cardId: " + cardId);
            }
        }
    }

    private Map<String, Long> importNoteNodes(List<NoteWordImportRequest.NoteNodeImportItem> noteNodes,
                                              NoteWordImportResult result) {
        Map<String, NoteWordImportRequest.NoteNodeImportItem> nodeMap = noteNodes.stream()
                .collect(Collectors.toMap(item -> normalizeKey(item.getNodeKey()), item -> item,
                        (a, b) -> a, LinkedHashMap::new));

        Map<String, Long> nodeKeyToId = new HashMap<>();

        // 优先复用历史记录
        for (String nodeKey : nodeMap.keySet()) {
            Optional<NoteNode> existing = noteNodeRepository.findByNoteKey(nodeKey);
            if (existing.isPresent()) {
                nodeKeyToId.put(nodeKey, existing.get().getId());
                result.getSummary().getNoteNodes().setReused(result.getSummary().getNoteNodes().getReused() + 1);
            }
        }

        Set<String> unresolved = new HashSet<>(nodeMap.keySet());
        unresolved.removeAll(nodeKeyToId.keySet());

        while (!unresolved.isEmpty()) {
            boolean progressed = false;
            List<String> toResolve = new ArrayList<>(unresolved);
            for (String nodeKey : toResolve) {
                NoteWordImportRequest.NoteNodeImportItem item = nodeMap.get(nodeKey);
                String parentNodeKey = normalizeKey(item.getParentNodeKey());
                Long parentId = null;
                if (parentNodeKey != null) {
                    parentId = nodeKeyToId.get(parentNodeKey);
                    if (parentId == null) {
                        // 父节点可能是历史数据但不在本次 noteNodes 中，尝试直接查询
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

    private void createNoteNode(String nodeKey, Long parentId, NoteWordImportRequest.NoteNodeImportItem item) {
        NoteNodeDTO source = item.getNoteNode() == null ? new NoteNodeDTO() : item.getNoteNode();
        NoteNodeDTO noteNodeDTO = new NoteNodeDTO();
        noteNodeDTO.setNoteKey(nodeKey);
        noteNodeDTO.setParentId(parentId);
        noteNodeDTO.setTitle(source.getTitle());
        noteNodeDTO.setNoteType(source.getNoteType());
        noteNodeDTO.setSort(source.getSort() == null ? 0 : source.getSort());
        noteNodeDTO.setMeta(resolveNoteNodeMeta(source.getMeta()));

        NoteNodeAddDTO addDTO = new NoteNodeAddDTO();
        addDTO.setNoteNode(noteNodeDTO);
        addDTO.setContent(item.getContent());
        noteNodeDomainService.add(addDTO);
    }

    private NoteNodeMetaDTO resolveNoteNodeMeta(NoteNodeMetaDTO sourceMeta) {
        if (sourceMeta == null) {
            return null;
        }

        NoteNodeMetaDTO target = new NoteNodeMetaDTO();
        target.setIcon(sourceMeta.getIcon());
        target.setSubject(sourceMeta.getSubject());

        List<NoteTagDTO> resolvedTags = new ArrayList<>();
        if (!CollectionUtils.isEmpty(sourceMeta.getTags())) {
            for (NoteTagDTO tag : sourceMeta.getTags()) {
                if (tag == null) {
                    continue;
                }
                String bizType = normalizeKey(tag.getBizType());
                String label = normalizeKey(tag.getLabel());
                if (bizType == null || label == null) {
                    continue;
                }

                NoteTag savedTag = noteTagRepository.findByBizTypeAndLabel(bizType, label)
                        .orElseGet(() -> {
                            NoteTag newTag = new NoteTag();
                            newTag.setBizType(bizType);
                            newTag.setLabel(label);
                            newTag.setClassName(tag.getClassName());
                            return noteTagRepository.save(newTag);
                        });

                NoteTagDTO resolved = new NoteTagDTO();
                resolved.setId(savedTag.getId());
                resolved.setBizType(savedTag.getBizType());
                resolved.setLabel(savedTag.getLabel());
                resolved.setClassName(savedTag.getClassName());
                resolvedTags.add(resolved);
            }
        }
        target.setTags(resolvedTags);
        return target;
    }

    private Map<String, Long> importWordCards(List<WordCardVO> wordCards, NoteWordImportResult result) {
        Map<String, Long> cardIdToWordCardId = new HashMap<>();
        for (WordCardVO wordCard : wordCards) {
            String cardId = normalizeKey(wordCard.getId());
            Optional<WordCard> existing = wordCardRepository.findByCardCode(cardId);
            if (existing.isPresent()) {
                syncExistingWordCardExamples(existing.get(), wordCard);
                cardIdToWordCardId.put(cardId, existing.get().getId());
                result.getSummary().getWordCards().setReused(result.getSummary().getWordCards().getReused() + 1);
                continue;
            }

            wordCardDomainService.add(wordCard);
            Long createdId = wordCardRepository.findByCardCode(cardId)
                    .map(WordCard::getId)
                    .orElseThrow(() -> new IllegalStateException("Created wordCard not found by cardId: " + cardId));
            cardIdToWordCardId.put(cardId, createdId);
            result.getSummary().getWordCards().setCreated(result.getSummary().getWordCards().getCreated() + 1);
        }
        return cardIdToWordCardId;
    }

    private void syncExistingWordCardExamples(WordCard existingCard, WordCardVO inputCard) {
        if (existingCard == null || inputCard == null || inputCard.getSections() == null
                || inputCard.getSections().getExamples() == null
                || CollectionUtils.isEmpty(inputCard.getSections().getExamples().getItems())) {
            return;
        }

        List<WordCardVO.ExampleItem> items = inputCard.getSections().getExamples().getItems()
                .stream()
                .filter(item -> item != null && normalizeKey(item.getId()) != null)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(items)) {
            return;
        }

        Set<Long> linkedIds = existingCard.getExamples() == null
                ? new HashSet<>()
                : existingCard.getExamples().stream()
                .map(ExampleSentence::getId)
                .collect(Collectors.toSet());

        List<String> codes = items.stream()
                .map(WordCardVO.ExampleItem::getId)
                .map(this::normalizeKey)
                .collect(Collectors.toList());
        Map<String, ExampleSentence> existingByCode = exampleSentenceRepository.findByExampleCodeIn(codes)
                .stream()
                .collect(Collectors.toMap(ExampleSentence::getExampleCode, item -> item, (a, b) -> a));

        int weight = 100;
        boolean changed = false;
        for (WordCardVO.ExampleItem item : items) {
            String code = normalizeKey(item.getId());
            if (code == null) {
                continue;
            }

            ExampleSentence example = existingByCode.get(code);
            if (example == null) {
                example = new ExampleSentence();
                example.setExampleCode(code);
                example.setSentence(item.getSentence());
                example.setMetaInfo(toExampleMetaInfo(item.getExplain()));
                example.setWeight(weight);
                example = exampleSentenceRepository.save(example);
                existingByCode.put(code, example);
            }

            if (!linkedIds.contains(example.getId())) {
                existingCard.getExamples().add(example);
                linkedIds.add(example.getId());
                changed = true;
            }
            weight++;
        }

        if (changed) {
            wordCardRepository.save(existingCard);
        }
    }

    private ExampleSentenceMetaInfo toExampleMetaInfo(WordCardVO.ExampleExplain source) {
        ExampleSentenceMetaInfo target = new ExampleSentenceMetaInfo();
        if (source == null) {
            return target;
        }
        target.setReading(source.getReading());
        target.setRomaji(source.getRomaji());
        target.setMeaningZh(source.getMeaningZh());

        if (!CollectionUtils.isEmpty(source.getWordGrammarBreakdown())) {
            List<ExampleSentenceMetaInfo.WordGrammarBreakdownItem> breakdown = source.getWordGrammarBreakdown().stream()
                    .filter(item -> item != null && normalizeKey(item.getWord()) != null)
                    .map(item -> {
                        ExampleSentenceMetaInfo.WordGrammarBreakdownItem mapped = new ExampleSentenceMetaInfo.WordGrammarBreakdownItem();
                        mapped.setWord(item.getWord());
                        mapped.setKana(item.getKana());
                        mapped.setDesc(item.getDesc());
                        return mapped;
                    })
                    .collect(Collectors.toList());
            target.setWordGrammarBreakdown(breakdown);
        }

        ExampleSentenceMetaInfo.FixedPattern fixedPattern = new ExampleSentenceMetaInfo.FixedPattern();
        if (source.getFixedPattern() != null) {
            fixedPattern.setPattern(source.getFixedPattern().getPattern());
            fixedPattern.setMeaningZh(source.getFixedPattern().getMeaningZh());
        }
        target.setFixedPattern(fixedPattern);
        return target;
    }

    private void importRelationsToLastNoteNode(List<NoteWordImportRequest.NoteNodeImportItem> noteNodes,
                                               Map<String, Long> nodeKeyToId,
                                               Map<String, Long> cardIdToWordCardId,
                                               NoteWordImportResult result) {
        if (CollectionUtils.isEmpty(cardIdToWordCardId)) {
            return;
        }

        if (CollectionUtils.isEmpty(noteNodes)) {
            throw new IllegalArgumentException("noteNodes cannot be empty when wordCards exist");
        }

        NoteWordImportRequest.NoteNodeImportItem lastNode = noteNodes.get(noteNodes.size() - 1);
        String lastNodeKey = normalizeKey(lastNode == null ? null : lastNode.getNodeKey());
        if (lastNodeKey == null) {
            throw new IllegalArgumentException("Last noteNodes.nodeKey cannot be blank");
        }
        Long noteNodeId = nodeKeyToId.get(lastNodeKey);
        if (noteNodeId == null) {
            throw new IllegalArgumentException("Last noteNode not found in import context: " + lastNodeKey);
        }

        for (Map.Entry<String, Long> entry : cardIdToWordCardId.entrySet()) {
            String cardId = entry.getKey();
            Long wordCardId = entry.getValue();
            if (wordCardId == null) {
                throw new IllegalArgumentException("Word card not found in import context: " + cardId);
            }

            boolean exists = wordCardNoteNodeRelRepository.findByWordCardId(wordCardId)
                    .stream()
                    .anyMatch(item -> item.getNoteNodeId().equals(noteNodeId));
            if (exists) {
                result.getSummary().getRelations().setReused(result.getSummary().getRelations().getReused() + 1);
                continue;
            }

            WordCardNoteNodeRel rel = new WordCardNoteNodeRel();
            rel.setWordCardId(wordCardId);
            rel.setNoteNodeId(noteNodeId);
            wordCardNoteNodeRelRepository.save(rel);
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
