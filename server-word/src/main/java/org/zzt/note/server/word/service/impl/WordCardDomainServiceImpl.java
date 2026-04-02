package org.zzt.note.server.word.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.athena.framework.data.jdbc.vo.PageInfo;
import org.athena.framework.data.jdbc.vo.PageResultVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.zzt.note.data.core.entity.NoteTag;
import org.zzt.note.data.core.repository.INoteTagRepository;
import org.zzt.note.server.word.entity.ExampleSentence;
import org.zzt.note.server.word.entity.UserWordProgress;
import org.zzt.note.server.word.entity.WordCard;
import org.zzt.note.server.word.entity.WordCardNoteNodeRel;
import org.zzt.note.server.word.entity.enums.UserWordProgressStatus;
import org.zzt.note.server.word.entity.meta.ExampleSentenceMetaInfo;
import org.zzt.note.server.word.entity.meta.UserWordProgressMetaInfo;
import org.zzt.note.server.word.entity.meta.WordCardMetaInfo;
import org.zzt.note.server.word.req.WordCardDomainPageRequest;
import org.zzt.note.server.word.repository.IExampleSentenceRepository;
import org.zzt.note.server.word.repository.IUserWordProgressRepository;
import org.zzt.note.server.word.repository.IWordCardNoteNodeRelRepository;
import org.zzt.note.server.word.repository.IWordCardRepository;
import org.zzt.note.server.word.service.IWordCardDomainService;
import org.zzt.note.server.word.vo.WordCardVO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 单词卡领域服务实现
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
@Service
@AllArgsConstructor
public class WordCardDomainServiceImpl implements IWordCardDomainService {

    private static final String WORD_CARD_TAG_BIZ_TYPE = "WORD_CARD";

    private final IWordCardNoteNodeRelRepository wordCardNoteNodeRelRepository;

    private final IWordCardRepository wordCardRepository;

    private final IExampleSentenceRepository exampleSentenceRepository;

    private final INoteTagRepository noteTagRepository;

    private final IUserWordProgressRepository userWordProgressRepository;

    @Override
    @Transactional
    public void add(WordCardVO wordCard) {
        if (wordCard == null) {
            throw new IllegalArgumentException("wordCard cannot be null");
        }
        if (wordCard.getId() == null || wordCard.getId().isBlank()) {
            throw new IllegalArgumentException("wordCard.id cannot be blank");
        }
        if (wordCard.getWord() == null || wordCard.getWord().getText() == null || wordCard.getWord().getText().isBlank()) {
            throw new IllegalArgumentException("wordCard.word.text cannot be blank");
        }
        if (wordCardRepository.findByCardCode(wordCard.getId()).isPresent()) {
            throw new IllegalArgumentException("WordCard already exists, cardCode=" + wordCard.getId());
        }

        WordCard entity = new WordCard();
        entity.setCardCode(wordCard.getId());
        entity.setWordText(wordCard.getWord().getText());
        entity.setLocale(extractLocale(wordCard.getId()));
        entity.setMetaInfo(toWordCardMetaInfo(wordCard.getSections()));
        entity.setTags(resolveTags(wordCard.getTags()));
        entity.setExamples(resolveExamples(wordCard.getSections()));

        wordCardRepository.save(entity);
    }

    @Override
    @Transactional
    public WordCardVO get(Long noteId, int index, Long userId) {
        if (noteId == null) {
            throw new IllegalArgumentException("noteId cannot be null");
        }
        if (index < 0) {
            throw new IllegalArgumentException("index cannot be less than 0");
        }

        List<WordCardNoteNodeRel> relations = wordCardNoteNodeRelRepository.findByNoteNodeIdOrderByWordCardIdAsc(noteId);
        if (CollectionUtils.isEmpty(relations) || index >= relations.size()) {
            throw new IllegalArgumentException("WordCard not found, noteId=" + noteId + ", index=" + index);
        }

        Long wordCardId = relations.get(index).getWordCardId();
        WordCard card = wordCardRepository.findById(wordCardId)
                .orElseThrow(() -> new IllegalArgumentException("WordCard not found, id=" + wordCardId));

        UserWordProgress progress = resolveProgress(userId, wordCardId);
        return toVO(card, progress);
    }

    @Override
    @Transactional
    public PageResultVO<WordCardVO> page(WordCardDomainPageRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request cannot be null");
        }
        if (request.getNoteId() == null) {
            throw new IllegalArgumentException("request.noteId cannot be null");
        }

        int currentPage = request.page() == null ? 1 : request.page();
        int size = request.size() == null ? 10 : request.size();
        if (currentPage < 1) {
            throw new IllegalArgumentException("request.page cannot be less than 1");
        }
        if (size < 1) {
            throw new IllegalArgumentException("request.size cannot be less than 1");
        }

        Pageable pageable = PageRequest.of(currentPage - 1, size);
        Page<WordCardNoteNodeRel> relationPage = wordCardNoteNodeRelRepository
                .findByNoteNodeIdOrderByWordCardIdAsc(request.getNoteId(), pageable);

        List<Long> wordCardIds = relationPage.getContent().stream()
                .map(WordCardNoteNodeRel::getWordCardId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<WordCardVO> list = buildPageVOList(wordCardIds, request.getUserId());
        PageInfo pageInfo = new PageInfo(relationPage.getTotalElements(), size, currentPage);
        return PageResultVO.ok(list, pageInfo);
    }

    @Override
    @Transactional
    public void delete(String cardId) {
        if (cardId == null || cardId.isBlank()) {
            throw new IllegalArgumentException("cardId cannot be blank");
        }

        WordCard card = wordCardRepository.findByCardCode(cardId)
                .orElseThrow(() -> new IllegalArgumentException("WordCard not found, cardCode=" + cardId));

        wordCardNoteNodeRelRepository.deleteByWordCardId(card.getId());

        card.getTags().clear();
        card.getExamples().clear();
        wordCardRepository.save(card);

        wordCardRepository.delete(card);
    }

    private String extractLocale(String cardCode) {
        if (cardCode == null || cardCode.isBlank()) {
            return "jp";
        }
        String[] parts = cardCode.split("-");
        if (parts.length == 0 || parts[0].isBlank()) {
            return "jp";
        }
        return parts[0].toLowerCase();
    }

    private WordCardMetaInfo toWordCardMetaInfo(WordCardVO.Sections sections) {
        WordCardMetaInfo metaInfo = new WordCardMetaInfo();
        if (sections == null) {
            return metaInfo;
        }
        metaInfo.setMeaning(toMeaningMetaSection(sections.getMeaning()));
        metaInfo.setSynonyms(toVocabularyMetaSection(sections.getSynonyms()));
        metaInfo.setRelated(toVocabularyMetaSection(sections.getRelated()));
        return metaInfo;
    }

    private WordCardMetaInfo.MeaningSection toMeaningMetaSection(WordCardVO.MeaningSection source) {
        WordCardMetaInfo.MeaningSection target = new WordCardMetaInfo.MeaningSection();
        if (source == null) {
            return target;
        }
        target.setCollapsedByDefault(source.getCollapsedByDefault());
        target.setDescription(source.getDescription());

        WordCardMetaInfo.MeaningMeta targetMeta = new WordCardMetaInfo.MeaningMeta();
        if (source.getMeta() != null) {
            targetMeta.setKana(source.getMeta().getKana());
            targetMeta.setZh(source.getMeta().getZh());
            targetMeta.setRomaji(source.getMeta().getRomaji());
        }
        target.setMeta(targetMeta);
        return target;
    }

    private WordCardMetaInfo.VocabularySection toVocabularyMetaSection(WordCardVO.VocabularySection source) {
        WordCardMetaInfo.VocabularySection target = new WordCardMetaInfo.VocabularySection();
        if (source == null || CollectionUtils.isEmpty(source.getItems())) {
            return target;
        }
        List<WordCardMetaInfo.VocabularyItem> items = source.getItems().stream()
                .filter(Objects::nonNull)
                .map(item -> {
                    WordCardMetaInfo.VocabularyItem vo = new WordCardMetaInfo.VocabularyItem();
                    vo.setText(item.getText());
                    vo.setKana(item.getKana());
                    return vo;
                })
                .collect(Collectors.toList());
        target.setItems(items);
        return target;
    }

    private List<NoteTag> resolveTags(List<WordCardVO.TagInfo> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return new ArrayList<>();
        }

        Map<String, String> classNameByLabel = new LinkedHashMap<>();
        for (WordCardVO.TagInfo tag : tags) {
            if (tag == null || tag.getName() == null || tag.getName().isBlank()) {
                continue;
            }
            classNameByLabel.putIfAbsent(tag.getName(), tag.getClassName());
        }
        if (classNameByLabel.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> labels = new ArrayList<>(classNameByLabel.keySet());
        List<NoteTag> existing = noteTagRepository.findByBizTypeAndLabelIn(WORD_CARD_TAG_BIZ_TYPE, labels);
        Map<String, NoteTag> existingByLabel = existing.stream()
                .collect(Collectors.toMap(NoteTag::getLabel, item -> item, (a, b) -> a, LinkedHashMap::new));

        List<NoteTag> result = new ArrayList<>();
        for (String label : labels) {
            NoteTag found = existingByLabel.get(label);
            if (found != null) {
                result.add(found);
                continue;
            }
            NoteTag created = new NoteTag();
            created.setBizType(WORD_CARD_TAG_BIZ_TYPE);
            created.setLabel(label);
            created.setClassName(classNameByLabel.get(label));
            result.add(created);
        }
        return result;
    }

    private List<ExampleSentence> resolveExamples(WordCardVO.Sections sections) {
        if (sections == null || sections.getExamples() == null || CollectionUtils.isEmpty(sections.getExamples().getItems())) {
            return new ArrayList<>();
        }

        List<WordCardVO.ExampleItem> inputItems = sections.getExamples().getItems().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (inputItems.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> nonBlankCodes = inputItems.stream()
                .map(WordCardVO.ExampleItem::getId)
                .filter(Objects::nonNull)
                .filter(code -> !code.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<ExampleSentence> existing = nonBlankCodes.isEmpty()
                ? List.of()
                : exampleSentenceRepository.findByExampleCodeIn(new ArrayList<>(nonBlankCodes));
        Map<String, ExampleSentence> existingByCode = existing.stream()
                .collect(Collectors.toMap(ExampleSentence::getExampleCode, item -> item, (a, b) -> a, LinkedHashMap::new));

        List<ExampleSentence> result = new ArrayList<>();
        int weight = 100;
        for (WordCardVO.ExampleItem input : inputItems) {
            String exampleCode = input.getId();
            ExampleSentence example = (exampleCode == null || exampleCode.isBlank())
                    ? new ExampleSentence()
                    : existingByCode.getOrDefault(exampleCode, new ExampleSentence());

            if (exampleCode != null && !exampleCode.isBlank()) {
                example.setExampleCode(exampleCode);
            } else {
                example.setExampleCode("auto-" + System.nanoTime() + "-" + weight);
            }
            example.setSentence(input.getSentence());
            example.setMetaInfo(toExampleMetaInfo(input.getExplain()));
            example.setWeight(weight++);
            result.add(example);
        }
        return result;
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
                    .filter(Objects::nonNull)
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

    private List<WordCardVO> buildPageVOList(List<Long> wordCardIds, Long userId) {
        if (CollectionUtils.isEmpty(wordCardIds)) {
            return List.of();
        }
        List<WordCard> cards = wordCardRepository.findByIdIn(wordCardIds);
        Map<Long, WordCard> cardById = cards.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(WordCard::getId, item -> item, (a, b) -> a, LinkedHashMap::new));

        Map<Long, UserWordProgress> progressByWordCardId = resolveProgressMap(userId, wordCardIds);
        List<WordCardVO> result = new ArrayList<>();
        for (Long wordCardId : wordCardIds) {
            WordCard card = cardById.get(wordCardId);
            if (card == null) {
                continue;
            }
            result.add(toVO(card, progressByWordCardId.get(wordCardId)));
        }
        return result;
    }

    private UserWordProgress resolveProgress(Long userId, Long wordCardId) {
        if (userId == null || wordCardId == null) {
            return null;
        }
        return userWordProgressRepository.findByUserIdAndWordCard_Id(userId, wordCardId).orElse(null);
    }

    private Map<Long, UserWordProgress> resolveProgressMap(Long userId, List<Long> wordCardIds) {
        if (userId == null || CollectionUtils.isEmpty(wordCardIds)) {
            return Map.of();
        }
        return userWordProgressRepository.findByUserIdAndWordCard_IdIn(userId, wordCardIds).stream()
                .filter(Objects::nonNull)
                .filter(progress -> progress.getWordCard() != null && progress.getWordCard().getId() != null)
                .collect(Collectors.toMap(progress -> progress.getWordCard().getId(), item -> item, (a, b) -> a, LinkedHashMap::new));
    }

    private WordCardVO toVO(WordCard card, UserWordProgress progress) {
        WordCardVO vo = new WordCardVO();
        vo.setId(card.getCardCode());

        WordCardVO.WordInfo word = new WordCardVO.WordInfo();
        word.setText(card.getWordText());
        word.setLevel(extractLevel(card.getTags()));
        vo.setWord(word);

        vo.setTags(toTagInfos(card.getTags()));
        vo.setSections(toSections(card));
        vo.setActions(defaultActions());
        vo.setProgress(toProgressInfo(progress));
        vo.setDone(Boolean.TRUE.equals(vo.getProgress().getDone()));
        return vo;
    }

    private WordCardVO.ProgressInfo toProgressInfo(UserWordProgress progress) {
        WordCardVO.ProgressInfo info = new WordCardVO.ProgressInfo();
        if (progress == null) {
            return info;
        }

        UserWordProgressStatus status = progress.getStatus() == null ? UserWordProgressStatus.NEW : progress.getStatus();
        info.setStatus(status.name());
        info.setDone(UserWordProgressStatus.MASTERED.equals(status));
        info.setLastReviewedAt(progress.getLastReviewedAt());

        UserWordProgressMetaInfo metaInfo = progress.getMetaInfo();
        if (metaInfo != null) {
            info.setHard(Boolean.TRUE.equals(metaInfo.getHard()));
            info.setFavorite(Boolean.TRUE.equals(metaInfo.getFavorite()));
            info.setReviewCount(metaInfo.getReviewCount() == null ? 0 : metaInfo.getReviewCount());
            info.setCorrectCount(metaInfo.getCorrectCount() == null ? 0 : metaInfo.getCorrectCount());
            info.setWrongCount(metaInfo.getWrongCount() == null ? 0 : metaInfo.getWrongCount());
        }
        return info;
    }

    private WordCardVO.Sections toSections(WordCard card) {
        WordCardVO.Sections sections = new WordCardVO.Sections();
        WordCardMetaInfo metaInfo = card.getMetaInfo();
        if (metaInfo != null) {
            sections.setMeaning(toMeaningSection(metaInfo.getMeaning()));
            sections.setSynonyms(toVocabularySection(metaInfo.getSynonyms()));
            sections.setRelated(toVocabularySection(metaInfo.getRelated()));
        }
        sections.setExamples(toExamplesSection(card.getExamples()));
        return sections;
    }

    private WordCardVO.MeaningSection toMeaningSection(WordCardMetaInfo.MeaningSection source) {
        WordCardVO.MeaningSection target = new WordCardVO.MeaningSection();
        if (source == null) {
            return target;
        }

        target.setCollapsedByDefault(source.getCollapsedByDefault());
        target.setDescription(source.getDescription());

        WordCardVO.MeaningMeta meta = new WordCardVO.MeaningMeta();
        if (source.getMeta() != null) {
            meta.setKana(source.getMeta().getKana());
            meta.setZh(source.getMeta().getZh());
            meta.setRomaji(source.getMeta().getRomaji());
        }
        target.setMeta(meta);
        return target;
    }

    private WordCardVO.VocabularySection toVocabularySection(WordCardMetaInfo.VocabularySection source) {
        WordCardVO.VocabularySection target = new WordCardVO.VocabularySection();
        if (source == null || CollectionUtils.isEmpty(source.getItems())) {
            return target;
        }

        List<WordCardVO.VocabularyItem> items = source.getItems().stream()
                .filter(Objects::nonNull)
                .map(item -> new WordCardVO.VocabularyItem(item.getText(), item.getKana()))
                .collect(Collectors.toList());
        target.setItems(items);
        return target;
    }

    private WordCardVO.ExamplesSection toExamplesSection(List<ExampleSentence> examples) {
        WordCardVO.ExamplesSection section = new WordCardVO.ExamplesSection();
        if (CollectionUtils.isEmpty(examples)) {
            return section;
        }

        List<WordCardVO.ExampleItem> items = examples.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ExampleSentence::getWeight, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(ExampleSentence::getId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toExampleItem)
                .collect(Collectors.toList());
        section.setItems(items);
        return section;
    }

    private WordCardVO.ExampleItem toExampleItem(ExampleSentence example) {
        WordCardVO.ExampleItem item = new WordCardVO.ExampleItem();
        item.setId(example.getExampleCode());
        item.setSentence(example.getSentence());
        item.setExplain(toExampleExplain(example.getMetaInfo()));
        return item;
    }

    private WordCardVO.ExampleExplain toExampleExplain(ExampleSentenceMetaInfo source) {
        WordCardVO.ExampleExplain target = new WordCardVO.ExampleExplain();
        if (source == null) {
            return target;
        }
        target.setReading(source.getReading());
        target.setRomaji(source.getRomaji());
        target.setMeaningZh(source.getMeaningZh());

        if (!CollectionUtils.isEmpty(source.getWordGrammarBreakdown())) {
            List<WordCardVO.WordGrammarBreakdownItem> breakdown = source.getWordGrammarBreakdown()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(item -> new WordCardVO.WordGrammarBreakdownItem(item.getWord(), item.getKana(), item.getDesc()))
                    .collect(Collectors.toList());
            target.setWordGrammarBreakdown(breakdown);
        }

        ExampleSentenceMetaInfo.FixedPattern sourcePattern = source.getFixedPattern();
        if (sourcePattern != null) {
            target.setFixedPattern(new WordCardVO.FixedPattern(sourcePattern.getPattern(), sourcePattern.getMeaningZh()));
        }
        return target;
    }

    private List<WordCardVO.TagInfo> toTagInfos(List<NoteTag> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return List.of();
        }
        return tags.stream()
                .filter(Objects::nonNull)
                .map(tag -> new WordCardVO.TagInfo(tag.getLabel(), tag.getClassName()))
                .collect(Collectors.toList());
    }

    private String extractLevel(List<NoteTag> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return null;
        }
        return tags.stream()
                .filter(Objects::nonNull)
                .map(NoteTag::getLabel)
                .filter(Objects::nonNull)
                .filter(label -> label.matches("N\\d+"))
                .findFirst()
                .orElse(null);
    }

    private List<WordCardVO.ActionInfo> defaultActions() {
        return List.of(
                new WordCardVO.ActionInfo("done", "✓", "完成"),
                new WordCardVO.ActionInfo("hard", "⚠", "易错"),
                new WordCardVO.ActionInfo("favorite", "★", "收藏"),
                new WordCardVO.ActionInfo("audio", "🔊", "发音"),
                new WordCardVO.ActionInfo("next", "⏭", "下一个")
        );
    }
}
