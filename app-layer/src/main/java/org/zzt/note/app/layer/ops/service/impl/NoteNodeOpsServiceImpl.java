package org.zzt.note.app.layer.ops.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.zzt.note.app.layer.ops.dto.NoteNodeOpsDeleteResult;
import org.zzt.note.app.layer.ops.service.INoteNodeOpsService;
import org.zzt.note.data.core.entity.NoteNode;
import org.zzt.note.data.core.repository.INoteNodeRepository;
import org.zzt.note.data.core.request.NoteNodeRequest;
import org.zzt.note.data.core.service.INoteNodeDomainService;
import org.zzt.note.server.word.entity.ExampleSentence;
import org.zzt.note.server.word.entity.WordCard;
import org.zzt.note.server.word.entity.WordCardNoteNodeRel;
import org.zzt.note.server.word.repository.IExampleSentenceRepository;
import org.zzt.note.server.word.repository.IWordCardNoteNodeRelRepository;
import org.zzt.note.server.word.repository.IWordCardRepository;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 节点运维服务。
 */
@Service
@AllArgsConstructor
public class NoteNodeOpsServiceImpl implements INoteNodeOpsService {

    private static final String CHECK_CODE = "root";

    private final INoteNodeRepository noteNodeRepository;

    private final INoteNodeDomainService noteNodeDomainService;

    private final IWordCardRepository wordCardRepository;

    private final IWordCardNoteNodeRelRepository wordCardNoteNodeRelRepository;

    private final IExampleSentenceRepository exampleSentenceRepository;

    private final EntityManager entityManager;

    @Override
    @Transactional
    public NoteNodeOpsDeleteResult deleteWithRelations(Long noteNodeId, String checkCode) {
        if (noteNodeId == null) {
            throw new IllegalArgumentException("noteNodeId cannot be null");
        }
        if (!CHECK_CODE.equals(checkCode)) {
            throw new IllegalArgumentException("checkCode invalid");
        }
        if (noteNodeRepository.findById(noteNodeId).isEmpty()) {
            throw new IllegalArgumentException("NoteNode not found, id=" + noteNodeId);
        }

        Set<Long> collectedSubtreeNoteNodeIds = collectSubtreeIds(noteNodeId);
        final Set<Long> subtreeNoteNodeIds = CollectionUtils.isEmpty(collectedSubtreeNoteNodeIds)
                ? Set.of(noteNodeId)
                : collectedSubtreeNoteNodeIds;

        Set<Long> relatedWordCardIds = collectRelatedWordCardIds(subtreeNoteNodeIds);
        Set<Long> deletableWordCardIds = new HashSet<>();
        int keptWordCardsShared = 0;

        for (Long wordCardId : relatedWordCardIds) {
            List<WordCardNoteNodeRel> allRels = wordCardNoteNodeRelRepository.findByWordCardId(wordCardId);
            boolean linkedOutsideSubtree = allRels.stream()
                    .map(WordCardNoteNodeRel::getNoteNodeId)
                    .anyMatch(noteId -> !subtreeNoteNodeIds.contains(noteId));
            if (linkedOutsideSubtree) {
                keptWordCardsShared++;
            } else {
                deletableWordCardIds.add(wordCardId);
            }
        }

        for (Long nodeId : subtreeNoteNodeIds) {
            wordCardNoteNodeRelRepository.deleteByNoteNodeId(nodeId);
        }

        Set<Long> touchedExampleIds = new HashSet<>();
        int deletedWordCards = 0;
        for (Long wordCardId : deletableWordCardIds) {
            Optional<WordCard> cardOpt = wordCardRepository.findById(wordCardId);
            if (cardOpt.isEmpty()) {
                continue;
            }
            WordCard card = cardOpt.get();

            if (!CollectionUtils.isEmpty(card.getExamples())) {
                touchedExampleIds.addAll(card.getExamples().stream()
                        .map(ExampleSentence::getId)
                        .collect(Collectors.toSet()));
            }

            card.getExamples().clear();
            card.getTags().clear();
            wordCardRepository.save(card);
            wordCardRepository.delete(card);
            deletedWordCards++;
        }

        int deletedExamples = 0;
        int keptExamplesShared = 0;
        for (Long exampleId : touchedExampleIds) {
            if (countExampleLinks(exampleId) > 0) {
                keptExamplesShared++;
                continue;
            }
            Optional<ExampleSentence> exampleOpt = exampleSentenceRepository.findById(exampleId);
            if (exampleOpt.isPresent()) {
                exampleSentenceRepository.delete(exampleOpt.get());
                deletedExamples++;
            }
        }

        NoteNodeRequest request = new NoteNodeRequest();
        request.setId(noteNodeId);
        noteNodeDomainService.delete(request);

        NoteNodeOpsDeleteResult result = new NoteNodeOpsDeleteResult();
        result.setRootNoteNodeId(noteNodeId);
        result.setTargetedNoteNodes(subtreeNoteNodeIds.size());
        result.setDeletedWordCards(deletedWordCards);
        result.setKeptWordCardsShared(keptWordCardsShared);
        result.setDeletedExamples(deletedExamples);
        result.setKeptExamplesShared(keptExamplesShared);
        return result;
    }

    private Set<Long> collectSubtreeIds(Long rootId) {
        List<NoteNode> allNodes = noteNodeRepository.findAll();
        if (CollectionUtils.isEmpty(allNodes)) {
            return new HashSet<>();
        }

        Map<Long, List<Long>> childrenMap = new HashMap<>();
        for (NoteNode node : allNodes) {
            if (node.getParentId() != null) {
                childrenMap.computeIfAbsent(node.getParentId(), k -> new ArrayList<>()).add(node.getId());
            }
        }

        Set<Long> subtreeIds = new HashSet<>();
        ArrayDeque<Long> queue = new ArrayDeque<>();
        queue.add(rootId);
        while (!queue.isEmpty()) {
            Long current = queue.poll();
            if (!subtreeIds.add(current)) {
                continue;
            }
            List<Long> children = childrenMap.getOrDefault(current, List.of());
            queue.addAll(children);
        }
        return subtreeIds;
    }

    private Set<Long> collectRelatedWordCardIds(Set<Long> noteNodeIds) {
        Set<Long> wordCardIds = new HashSet<>();
        for (Long noteNodeId : noteNodeIds) {
            List<WordCardNoteNodeRel> relations = wordCardNoteNodeRelRepository.findByNoteNodeId(noteNodeId);
            for (WordCardNoteNodeRel relation : relations) {
                wordCardIds.add(relation.getWordCardId());
            }
        }
        return wordCardIds;
    }

    private long countExampleLinks(Long exampleId) {
        Object value = entityManager.createNativeQuery(
                        "select count(1) from word_card_example_rel where example_sentence_id = :exampleId")
                .setParameter("exampleId", exampleId)
                .getSingleResult();
        if (value instanceof BigInteger bigInteger) {
            return bigInteger.longValue();
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return 0L;
    }
}
