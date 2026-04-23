package org.zzt.note.server.word.article.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.athena.framework.data.jdbc.vo.PageInfo;
import org.athena.framework.data.jdbc.vo.PageResultVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.zzt.note.data.core.repository.INoteNodeRepository;
import org.zzt.note.server.word.article.entity.Article;
import org.zzt.note.server.word.article.entity.ArticleNoteNodeRel;
import org.zzt.note.server.word.article.entity.ArticleUserProgress;
import org.zzt.note.server.word.article.entity.meta.ArticleMetaInfo;
import org.zzt.note.server.word.article.repository.IArticleNoteNodeRelRepository;
import org.zzt.note.server.word.article.repository.IArticleRepository;
import org.zzt.note.server.word.article.repository.IArticleUserProgressRepository;
import org.zzt.note.server.word.article.req.ArticleDomainPageRequest;
import org.zzt.note.server.word.article.service.IArticleDomainService;
import org.zzt.note.server.word.article.vo.ArticleReaderVO;
import org.zzt.note.server.word.article.vo.ArticleVO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 文章阅读领域服务实现
 *
 * @author zhouzhitong
 * @since 2026/4/7
 */
@Service
@AllArgsConstructor
public class ArticleDomainServiceImpl implements IArticleDomainService {

    private static final long DEFAULT_USER_ID = 1L;

    private final IArticleRepository articleRepository;

    private final IArticleUserProgressRepository articleUserProgressRepository;

    private final IArticleNoteNodeRelRepository articleNoteNodeRelRepository;

    private final INoteNodeRepository noteNodeRepository;

    @Override
    @Transactional
    public void save(ArticleVO article) {
        if (article == null) {
            throw new IllegalArgumentException("article cannot be null");
        }
        if (!StringUtils.hasText(article.getId())) {
            throw new IllegalArgumentException("article.id cannot be blank");
        }
        if (!StringUtils.hasText(article.getTitle())) {
            throw new IllegalArgumentException("article.title cannot be blank");
        }

        Article entity = articleRepository.findByArticleCode(article.getId()).orElseGet(Article::new);
        entity.setArticleCode(article.getId());
        entity.setTitle(article.getTitle());
        entity.setMetaInfo(toMetaInfo(article));

        Article savedArticle = articleRepository.save(entity);
        upsertNoteNodeRelation(savedArticle.getId(), article.getNoteNodeId());
    }

    @Override
    @Transactional
    public ArticleVO get(String articleId) {
        if (!StringUtils.hasText(articleId)) {
            throw new IllegalArgumentException("articleId cannot be blank");
        }
        Article article = articleRepository.findByArticleCode(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article not found, articleId=" + articleId));

        ArticleUserProgress progress = resolveProgress(DEFAULT_USER_ID, article.getId(), false);
        Long noteNodeId = resolveNoteNodeId(article.getId());
        return toVO(article, progress, true, noteNodeId);
    }

    @Override
    @Transactional
    public ArticleReaderVO getByNoteNodeId(Long noteNodeId) {
        if (noteNodeId == null || noteNodeId <= 0) {
            throw new IllegalArgumentException("noteNodeId must be greater than 0");
        }

        List<ArticleNoteNodeRel> relations = articleNoteNodeRelRepository.findByNoteNodeIdOrderByArticleIdAsc(noteNodeId);
        if (relations.isEmpty()) {
            return new ArticleReaderVO(noteNodeId, 0, new ArrayList<>());
        }

        List<Long> articleIds = relations.stream()
                .map(ArticleNoteNodeRel::getArticleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (articleIds.isEmpty()) {
            return new ArticleReaderVO(noteNodeId, 0, new ArrayList<>());
        }

        Map<Long, Article> articleMap = articleRepository.findAllById(articleIds).stream()
                .collect(Collectors.toMap(Article::getId, item -> item));

        List<ArticleVO> articles = new ArrayList<>();
        for (ArticleNoteNodeRel rel : relations) {
            if (rel == null || rel.getArticleId() == null) {
                continue;
            }
            Article article = articleMap.get(rel.getArticleId());
            if (article == null) {
                continue;
            }
            ArticleUserProgress progress = resolveProgress(DEFAULT_USER_ID, article.getId(), false);
            articles.add(toVO(article, progress, true, noteNodeId));
        }

        return new ArticleReaderVO(noteNodeId, resolveCurrentArticleIndex(articles), articles);
    }

    @Override
    @Transactional
    public PageResultVO<ArticleVO> page(ArticleDomainPageRequest request) {
        int currentPage = request == null || request.page() == null ? 1 : request.page();
        int size = request == null || request.size() == null ? 10 : request.size();
        if (currentPage < 1) {
            throw new IllegalArgumentException("request.page cannot be less than 1");
        }
        if (size < 1) {
            throw new IllegalArgumentException("request.size cannot be less than 1");
        }

        Pageable pageable = PageRequest.of(currentPage - 1, size);
        String keyword = request == null ? null : request.getKeyword();
        Page<Article> result = StringUtils.hasText(keyword)
                ? articleRepository.findByTitleContainingOrderByIdDesc(keyword.trim(), pageable)
                : articleRepository.findAll(pageable);

        Map<Long, Long> noteNodeIdMap = resolveNoteNodeIdMap(result.getContent());
        List<ArticleVO> records = result.getContent().stream()
                .map(item -> toVO(
                        item,
                        resolveProgress(DEFAULT_USER_ID, item.getId(), false),
                        false,
                        noteNodeIdMap.get(item.getId())
                ))
                .collect(Collectors.toList());

        PageInfo pageInfo = new PageInfo(result.getTotalElements(), size, currentPage);
        return PageResultVO.ok(records, pageInfo);
    }

    @Override
    @Transactional
    public ArticleVO updateFavorite(String articleId, Boolean favorite) {
        Article article = requireArticle(articleId);
        ArticleUserProgress progress = resolveProgress(DEFAULT_USER_ID, article.getId(), true);
        progress.setFavorite(Boolean.TRUE.equals(favorite));
        ArticleUserProgress saved = articleUserProgressRepository.save(progress);
        return toVO(article, saved, true, resolveNoteNodeId(article.getId()));
    }

    @Override
    @Transactional
    public ArticleVO updatePlaybackRate(String articleId, Double playbackRate) {
        if (playbackRate == null || playbackRate <= 0D) {
            throw new IllegalArgumentException("playbackRate must be greater than 0");
        }

        Article article = requireArticle(articleId);
        ArticleUserProgress progress = resolveProgress(DEFAULT_USER_ID, article.getId(), true);
        progress.setPlaybackRate(BigDecimal.valueOf(playbackRate));
        ArticleUserProgress saved = articleUserProgressRepository.save(progress);
        return toVO(article, saved, true, resolveNoteNodeId(article.getId()));
    }

    @Override
    @Transactional
    public ArticleVO updatePosition(String articleId, Integer paragraphIndex) {
        if (paragraphIndex == null || paragraphIndex < 0) {
            throw new IllegalArgumentException("paragraphIndex cannot be less than 0");
        }

        Article article = requireArticle(articleId);
        ArticleUserProgress progress = resolveProgress(DEFAULT_USER_ID, article.getId(), true);
        progress.setLastReadParagraphIndex(paragraphIndex);
        ArticleUserProgress saved = articleUserProgressRepository.save(progress);
        return toVO(article, saved, true, resolveNoteNodeId(article.getId()));
    }

    @Override
    @Transactional
    public void delete(String articleId) {
        Article article = requireArticle(articleId);
        articleNoteNodeRelRepository.deleteByArticleId(article.getId());
        articleUserProgressRepository.deleteByArticleId(article.getId());
        articleRepository.delete(article);
    }

    private Article requireArticle(String articleId) {
        if (!StringUtils.hasText(articleId)) {
            throw new IllegalArgumentException("articleId cannot be blank");
        }
        return articleRepository.findByArticleCode(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article not found, articleId=" + articleId));
    }

    private ArticleMetaInfo toMetaInfo(ArticleVO article) {
        ArticleMetaInfo metaInfo = new ArticleMetaInfo();

        List<List<ArticleMetaInfo.TokenInfo>> paragraphList = new ArrayList<>();
        if (article.getParagraphs() != null) {
            for (List<ArticleVO.TokenInfo> paragraph : article.getParagraphs()) {
                List<ArticleMetaInfo.TokenInfo> tokens = new ArrayList<>();
                if (paragraph != null) {
                    for (ArticleVO.TokenInfo token : paragraph) {
                        if (token == null || !StringUtils.hasText(token.getText())) {
                            continue;
                        }
                        tokens.add(new ArticleMetaInfo.TokenInfo(token.getText(), token.getKana()));
                    }
                }
                if (!tokens.isEmpty()) {
                    paragraphList.add(tokens);
                }
            }
        }
        metaInfo.setParagraphs(paragraphList);

        List<String> translations = Optional.ofNullable(article.getTranslation())
                .orElseGet(ArrayList::new)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        metaInfo.setTranslation(translations);
        metaInfo.setKnowledge(toMetaKnowledge(article.getKnowledge()));

        return metaInfo;
    }

    private ArticleVO toVO(Article article, ArticleUserProgress progress, boolean includeContent, Long noteNodeId) {
        ArticleVO vo = new ArticleVO();
        vo.setId(article.getArticleCode());
        vo.setNoteNodeId(noteNodeId);
        vo.setTitle(article.getTitle());

        if (includeContent) {
            vo.setParagraphs(toVoParagraphs(article.getMetaInfo()));
            vo.setTranslation(toVoTranslation(article.getMetaInfo()));
            vo.setKnowledge(toVoKnowledge(article.getMetaInfo()));
        } else {
            vo.setParagraphs(new ArrayList<>());
            vo.setTranslation(new ArrayList<>());
            vo.setKnowledge(new ArticleVO.Knowledge());
        }

        ArticleVO.Progress progressVO = new ArticleVO.Progress();
        progressVO.setFavorite(progress != null && Boolean.TRUE.equals(progress.getFavorite()));
        progressVO.setCompleted(progress != null && Boolean.TRUE.equals(progress.getCompleted()));
        progressVO.setLastReadParagraphIndex(progress == null || progress.getLastReadParagraphIndex() == null
                ? 0
                : progress.getLastReadParagraphIndex());
        progressVO.setPlaybackRate(progress == null || progress.getPlaybackRate() == null
                ? 1.0D
                : progress.getPlaybackRate().doubleValue());
        vo.setProgress(progressVO);

        return vo;
    }

    private List<List<ArticleVO.TokenInfo>> toVoParagraphs(ArticleMetaInfo metaInfo) {
        if (metaInfo == null || metaInfo.getParagraphs() == null) {
            return new ArrayList<>();
        }

        return metaInfo.getParagraphs().stream()
                .map(paragraph -> {
                    if (paragraph == null) {
                        return new ArrayList<ArticleVO.TokenInfo>();
                    }
                    return paragraph.stream()
                            .filter(Objects::nonNull)
                            .map(token -> new ArticleVO.TokenInfo(token.getText(), token.getKana()))
                            .collect(Collectors.toCollection(ArrayList::new));
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<String> toVoTranslation(ArticleMetaInfo metaInfo) {
        if (metaInfo == null || metaInfo.getTranslation() == null) {
            return new ArrayList<>();
        }
        return metaInfo.getTranslation().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private ArticleUserProgress resolveProgress(Long userId, Long articleId, boolean createIfAbsent) {
        if (userId == null || userId <= 0 || articleId == null) {
            return null;
        }

        Optional<ArticleUserProgress> found = articleUserProgressRepository.findByUserIdAndArticleId(userId, articleId);
        if (found.isPresent()) {
            return found.get();
        }
        if (!createIfAbsent) {
            return null;
        }

        Article articleRef = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article not found, id=" + articleId));
        ArticleUserProgress created = new ArticleUserProgress();
        created.setUserId(userId);
        created.setArticle(articleRef);
        created.setFavorite(false);
        created.setCompleted(false);
        created.setLastReadParagraphIndex(0);
        created.setPlaybackRate(BigDecimal.valueOf(1.0D));
        return created;
    }

    private ArticleMetaInfo.KnowledgeInfo toMetaKnowledge(ArticleVO.Knowledge knowledge) {
        ArticleMetaInfo.KnowledgeInfo knowledgeInfo = new ArticleMetaInfo.KnowledgeInfo();
        if (knowledge == null) {
            return knowledgeInfo;
        }

        List<ArticleMetaInfo.CoreVocabularyInfo> coreVocabulary = Optional.ofNullable(knowledge.getCoreVocabulary())
                .orElseGet(ArrayList::new)
                .stream()
                .filter(Objects::nonNull)
                .map(item -> new ArticleMetaInfo.CoreVocabularyInfo(item.getJp(), item.getKana(), item.getMeaning()))
                .collect(Collectors.toCollection(ArrayList::new));
        knowledgeInfo.setCoreVocabulary(coreVocabulary);

        List<ArticleMetaInfo.CoreSentencePatternInfo> coreSentencePatterns = Optional.ofNullable(knowledge.getCoreSentencePatterns())
                .orElseGet(ArrayList::new)
                .stream()
                .filter(Objects::nonNull)
                .map(item -> new ArticleMetaInfo.CoreSentencePatternInfo(item.getJp(), item.getMeaning()))
                .collect(Collectors.toCollection(ArrayList::new));
        knowledgeInfo.setCoreSentencePatterns(coreSentencePatterns);

        return knowledgeInfo;
    }

    private ArticleVO.Knowledge toVoKnowledge(ArticleMetaInfo metaInfo) {
        ArticleVO.Knowledge knowledge = new ArticleVO.Knowledge();
        if (metaInfo == null || metaInfo.getKnowledge() == null) {
            return knowledge;
        }

        List<ArticleVO.CoreVocabulary> coreVocabulary = Optional.ofNullable(metaInfo.getKnowledge().getCoreVocabulary())
                .orElseGet(ArrayList::new)
                .stream()
                .filter(Objects::nonNull)
                .map(item -> new ArticleVO.CoreVocabulary(item.getJp(), item.getKana(), item.getMeaning()))
                .collect(Collectors.toCollection(ArrayList::new));
        knowledge.setCoreVocabulary(coreVocabulary);

        List<ArticleVO.CoreSentencePattern> coreSentencePatterns = Optional.ofNullable(metaInfo.getKnowledge().getCoreSentencePatterns())
                .orElseGet(ArrayList::new)
                .stream()
                .filter(Objects::nonNull)
                .map(item -> new ArticleVO.CoreSentencePattern(item.getJp(), item.getMeaning()))
                .collect(Collectors.toCollection(ArrayList::new));
        knowledge.setCoreSentencePatterns(coreSentencePatterns);

        return knowledge;
    }

    private Integer resolveCurrentArticleIndex(List<ArticleVO> articles) {
        if (articles == null || articles.isEmpty()) {
            return 0;
        }
        for (int i = 0; i < articles.size(); i += 1) {
            ArticleVO article = articles.get(i);
            boolean completed = article != null
                    && article.getProgress() != null
                    && Boolean.TRUE.equals(article.getProgress().getCompleted());
            if (!completed) {
                return i;
            }
        }
        return 0;
    }

    private void upsertNoteNodeRelation(Long articleId, Long noteNodeId) {
        if (articleId == null || noteNodeId == null) {
            return;
        }
        noteNodeRepository.findById(noteNodeId)
                .orElseThrow(() -> new IllegalArgumentException("NoteNode not found, id=" + noteNodeId));

        articleNoteNodeRelRepository.deleteByArticleId(articleId);
        ArticleNoteNodeRel rel = new ArticleNoteNodeRel();
        rel.setArticleId(articleId);
        rel.setNoteNodeId(noteNodeId);
        articleNoteNodeRelRepository.save(rel);
    }

    private Long resolveNoteNodeId(Long articleId) {
        if (articleId == null) {
            return null;
        }
        return articleNoteNodeRelRepository.findByArticleId(articleId).stream()
                .map(ArticleNoteNodeRel::getNoteNodeId)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private Map<Long, Long> resolveNoteNodeIdMap(List<Article> articles) {
        if (articles == null || articles.isEmpty()) {
            return Map.of();
        }
        List<Long> articleIds = articles.stream()
                .map(Article::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (articleIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Long> map = new LinkedHashMap<>();
        for (ArticleNoteNodeRel rel : articleNoteNodeRelRepository.findByArticleIdIn(articleIds)) {
            if (rel == null || rel.getArticleId() == null || rel.getNoteNodeId() == null) {
                continue;
            }
            map.putIfAbsent(rel.getArticleId(), rel.getNoteNodeId());
        }
        return map;
    }
}
