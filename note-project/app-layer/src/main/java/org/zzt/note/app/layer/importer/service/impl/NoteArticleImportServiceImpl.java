package org.zzt.note.app.layer.importer.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.zzt.note.app.layer.importer.dto.NoteArticleImportRequest;
import org.zzt.note.app.layer.importer.dto.NoteArticleImportResult;
import org.zzt.note.app.layer.importer.service.INoteArticleImportService;
import org.zzt.note.data.core.dto.NoteNodeAddDTO;
import org.zzt.note.data.core.entity.NoteNode;
import org.zzt.note.data.core.entity.dto.NoteNodeDTO;
import org.zzt.note.data.core.repository.INoteNodeRepository;
import org.zzt.note.data.core.service.INoteNodeDomainService;
import org.zzt.note.server.word.article.entity.Article;
import org.zzt.note.server.word.article.repository.IArticleNoteNodeRelRepository;
import org.zzt.note.server.word.article.repository.IArticleRepository;
import org.zzt.note.server.word.article.service.IArticleDomainService;
import org.zzt.note.server.word.article.vo.ArticleVO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 笔记节点 + 文章导入服务。
 */
@Service
@AllArgsConstructor
public class NoteArticleImportServiceImpl implements INoteArticleImportService {

    private final INoteNodeRepository noteNodeRepository;

    private final INoteNodeDomainService noteNodeDomainService;

    private final IArticleRepository articleRepository;

    private final IArticleDomainService articleDomainService;

    private final IArticleNoteNodeRelRepository articleNoteNodeRelRepository;

    @Override
    @Transactional
    public NoteArticleImportResult importData(NoteArticleImportRequest request) {
        NoteArticleImportRequest safeRequest = request == null ? new NoteArticleImportRequest() : request;
        NoteArticleImportRequest.Payload payload = safeRequest.getPayload() == null
                ? new NoteArticleImportRequest.Payload()
                : safeRequest.getPayload();

        List<NoteArticleImportRequest.NoteNodeImportItem> noteNodes = payload.getNoteNodes() == null
                ? List.of()
                : payload.getNoteNodes();
        List<ArticleVO> articles = payload.getArticles() == null ? List.of() : payload.getArticles();

        validateRequest(noteNodes, articles);

        boolean upsertEnabled = isUpsertEnabled(safeRequest);

        NoteArticleImportResult result = new NoteArticleImportResult();
        result.setImportId(safeRequest.getMeta() == null ? null : safeRequest.getMeta().getImportId());
        result.getSummary().getNoteNodes().setTotal(noteNodes.size());
        result.getSummary().getArticles().setTotal(articles.size());
        result.getSummary().getRelations().setTotal(articles.size());

        Map<String, Long> nodeKeyToId = importNoteNodes(noteNodes, result);
        Long defaultNoteNodeId = resolveDefaultNoteNodeId(noteNodes, nodeKeyToId);

        importArticles(articles, defaultNoteNodeId, upsertEnabled, result);
        return result;
    }

    private void validateRequest(List<NoteArticleImportRequest.NoteNodeImportItem> noteNodes,
                                 List<ArticleVO> articles) {
        Set<String> nodeKeys = new HashSet<>();
        for (NoteArticleImportRequest.NoteNodeImportItem node : noteNodes) {
            String nodeKey = normalize(node == null ? null : node.getNodeKey());
            if (nodeKey == null) {
                throw new IllegalArgumentException("noteNodes.nodeKey cannot be blank");
            }
            if (!nodeKeys.add(nodeKey)) {
                throw new IllegalArgumentException("Duplicate nodeKey in payload: " + nodeKey);
            }
        }

        Set<String> articleIds = new HashSet<>();
        for (ArticleVO article : articles) {
            String articleId = normalize(article == null ? null : article.getId());
            if (articleId == null) {
                throw new IllegalArgumentException("articles.id cannot be blank");
            }
            if (!articleIds.add(articleId)) {
                throw new IllegalArgumentException("Duplicate articles.id in payload: " + articleId);
            }
            if (!StringUtils.hasText(article == null ? null : article.getTitle())) {
                throw new IllegalArgumentException("articles.title cannot be blank, articleId: " + articleId);
            }
        }

        if (!CollectionUtils.isEmpty(articles) && CollectionUtils.isEmpty(noteNodes)) {
            boolean allHaveNoteNodeId = articles.stream().allMatch(item -> item != null && item.getNoteNodeId() != null);
            if (!allHaveNoteNodeId) {
                throw new IllegalArgumentException("When payload.noteNodes is empty, each article.noteNodeId is required");
            }
        }
    }

    private Map<String, Long> importNoteNodes(List<NoteArticleImportRequest.NoteNodeImportItem> noteNodes,
                                              NoteArticleImportResult result) {
        Map<String, NoteArticleImportRequest.NoteNodeImportItem> nodeMap = noteNodes.stream()
                .collect(Collectors.toMap(item -> normalize(item.getNodeKey()), item -> item,
                        (a, b) -> a, LinkedHashMap::new));

        Map<String, Long> nodeKeyToId = new HashMap<>();

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
                NoteArticleImportRequest.NoteNodeImportItem item = nodeMap.get(nodeKey);
                String parentNodeKey = normalize(item.getParentNodeKey());
                Long parentId = null;
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

    private void createNoteNode(String nodeKey, Long parentId, NoteArticleImportRequest.NoteNodeImportItem item) {
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

    private Long resolveDefaultNoteNodeId(List<NoteArticleImportRequest.NoteNodeImportItem> noteNodes,
                                          Map<String, Long> nodeKeyToId) {
        if (CollectionUtils.isEmpty(noteNodes)) {
            return null;
        }
        NoteArticleImportRequest.NoteNodeImportItem lastNode = noteNodes.get(noteNodes.size() - 1);
        String lastNodeKey = normalize(lastNode == null ? null : lastNode.getNodeKey());
        Long noteNodeId = lastNodeKey == null ? null : nodeKeyToId.get(lastNodeKey);
        if (noteNodeId == null) {
            throw new IllegalArgumentException("Last noteNode not found in import context: " + lastNodeKey);
        }
        return noteNodeId;
    }

    private void importArticles(List<ArticleVO> articles,
                                Long defaultNoteNodeId,
                                boolean upsertEnabled,
                                NoteArticleImportResult result) {
        for (ArticleVO article : articles) {
            String articleId = normalize(article.getId());
            Long targetNoteNodeId = article.getNoteNodeId() == null ? defaultNoteNodeId : article.getNoteNodeId();
            if (targetNoteNodeId == null) {
                throw new IllegalArgumentException("article.noteNodeId cannot be null, articleId=" + articleId);
            }

            Optional<Article> existing = articleRepository.findByArticleCode(articleId);
            boolean relationExisted = existing.isPresent() && relationExists(existing.get().getId(), targetNoteNodeId);

            if (existing.isPresent() && !upsertEnabled) {
                result.getSummary().getArticles().setReused(result.getSummary().getArticles().getReused() + 1);
                if (relationExisted) {
                    result.getSummary().getRelations().setReused(result.getSummary().getRelations().getReused() + 1);
                } else {
                    result.getSummary().getRelations().setSkipped(result.getSummary().getRelations().getSkipped() + 1);
                }
                continue;
            }

            ArticleVO input = new ArticleVO();
            input.setId(articleId);
            input.setNoteNodeId(targetNoteNodeId);
            input.setTitle(normalize(article.getTitle()));
            input.setParagraphs(sanitizeParagraphs(article.getParagraphs()));
            input.setTranslation(sanitizeTranslation(article.getTranslation()));
            input.setKnowledge(sanitizeKnowledge(article.getKnowledge()));
            input.setProgress(article.getProgress());

            articleDomainService.save(input);

            if (existing.isPresent()) {
                result.getSummary().getArticles().setUpdated(result.getSummary().getArticles().getUpdated() + 1);
            } else {
                result.getSummary().getArticles().setCreated(result.getSummary().getArticles().getCreated() + 1);
            }

            if (relationExisted) {
                result.getSummary().getRelations().setReused(result.getSummary().getRelations().getReused() + 1);
            } else {
                result.getSummary().getRelations().setCreated(result.getSummary().getRelations().getCreated() + 1);
            }
        }
    }

    private boolean relationExists(Long articleDbId, Long noteNodeId) {
        if (articleDbId == null || noteNodeId == null) {
            return false;
        }
        return articleNoteNodeRelRepository.findByArticleId(articleDbId)
                .stream()
                .anyMatch(rel -> rel != null && noteNodeId.equals(rel.getNoteNodeId()));
    }

    private boolean isUpsertEnabled(NoteArticleImportRequest request) {
        if (request == null || request.getMeta() == null || request.getMeta().getOptions() == null) {
            return true;
        }
        return !Boolean.FALSE.equals(request.getMeta().getOptions().getUpsert());
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private List<List<ArticleVO.TokenInfo>> sanitizeParagraphs(List<List<ArticleVO.TokenInfo>> paragraphs) {
        if (paragraphs == null) {
            return Collections.emptyList();
        }
        List<List<ArticleVO.TokenInfo>> result = new ArrayList<>();
        for (List<ArticleVO.TokenInfo> paragraph : paragraphs) {
            if (paragraph == null) {
                continue;
            }
            List<ArticleVO.TokenInfo> tokens = new ArrayList<>();
            for (ArticleVO.TokenInfo token : paragraph) {
                String text = normalize(token == null ? null : token.getText());
                if (text == null) {
                    continue;
                }
                tokens.add(new ArticleVO.TokenInfo(text, normalize(token.getKana())));
            }
            if (!tokens.isEmpty()) {
                result.add(tokens);
            }
        }
        return result;
    }

    private List<String> sanitizeTranslation(List<String> translation) {
        if (translation == null) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (String line : translation) {
            String normalized = normalize(line);
            if (normalized != null) {
                result.add(normalized);
            }
        }
        return result;
    }

    private ArticleVO.Knowledge sanitizeKnowledge(ArticleVO.Knowledge knowledge) {
        ArticleVO.Knowledge result = new ArticleVO.Knowledge();
        if (knowledge == null) {
            return result;
        }

        List<ArticleVO.CoreVocabulary> coreVocabulary = new ArrayList<>();
        for (ArticleVO.CoreVocabulary item : Optional.ofNullable(knowledge.getCoreVocabulary()).orElseGet(ArrayList::new)) {
            String jp = normalize(item == null ? null : item.getJp());
            if (jp == null) {
                continue;
            }
            coreVocabulary.add(new ArticleVO.CoreVocabulary(
                    jp,
                    normalize(item.getKana()),
                    normalize(item.getMeaning())
            ));
        }
        result.setCoreVocabulary(coreVocabulary);

        List<ArticleVO.CoreSentencePattern> coreSentencePatterns = new ArrayList<>();
        for (ArticleVO.CoreSentencePattern item : Optional.ofNullable(knowledge.getCoreSentencePatterns()).orElseGet(ArrayList::new)) {
            String jp = normalize(item == null ? null : item.getJp());
            if (jp == null) {
                continue;
            }
            coreSentencePatterns.add(new ArticleVO.CoreSentencePattern(
                    jp,
                    normalize(item.getMeaning())
            ));
        }
        result.setCoreSentencePatterns(coreSentencePatterns);

        return result;
    }
}
