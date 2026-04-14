package org.zzt.note.server.practice.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.athena.framework.data.jdbc.vo.PageInfo;
import org.athena.framework.data.jdbc.vo.PageResultVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.zzt.note.server.practice.entity.PracticeAnswerRecord;
import org.zzt.note.server.practice.entity.PracticeSession;
import org.zzt.note.server.practice.entity.PracticeSessionItem;
import org.zzt.note.server.practice.entity.Question;
import org.zzt.note.server.practice.entity.QuestionNoteNodeRel;
import org.zzt.note.server.practice.entity.UserQuestionProgress;
import org.zzt.note.server.practice.entity.enums.PracticeAnswerResult;
import org.zzt.note.server.practice.entity.enums.PracticeMode;
import org.zzt.note.server.practice.entity.enums.PracticeSessionStatus;
import org.zzt.note.server.practice.entity.enums.QuestionType;
import org.zzt.note.server.practice.entity.enums.UserQuestionProgressStatus;
import org.zzt.note.server.practice.entity.meta.QuestionMetaInfo;
import org.zzt.note.server.practice.repository.IPracticeAnswerRecordRepository;
import org.zzt.note.server.practice.repository.IPracticeSessionItemRepository;
import org.zzt.note.server.practice.repository.IPracticeSessionRepository;
import org.zzt.note.server.practice.repository.IQuestionNoteNodeRelRepository;
import org.zzt.note.server.practice.repository.IQuestionRepository;
import org.zzt.note.server.practice.repository.IUserQuestionProgressRepository;
import org.zzt.note.server.practice.req.CreatePracticeSessionRequest;
import org.zzt.note.server.practice.req.SubmitPracticeAnswerRequest;
import org.zzt.note.server.practice.service.IPracticeService;
import org.zzt.note.server.practice.utils.PracticeUserUtils;
import org.zzt.note.server.practice.vo.CreatePracticeSessionResponse;
import org.zzt.note.server.practice.vo.GetPracticeItemResponse;
import org.zzt.note.server.practice.vo.NoteNodePracticeStatsVO;
import org.zzt.note.server.practice.vo.PracticeQuestionVO;
import org.zzt.note.server.practice.vo.PracticeSessionVO;
import org.zzt.note.server.practice.vo.SubmitPracticeAnswerResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 做题练习服务实现
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
@Service
@AllArgsConstructor
public class PracticeServiceImpl implements IPracticeService {

    private static final int DEFAULT_MASTERED_STREAK = 3;

    private final IQuestionRepository questionRepository;
    private final IQuestionNoteNodeRelRepository relRepository;
    private final IPracticeSessionRepository sessionRepository;
    private final IPracticeSessionItemRepository sessionItemRepository;
    private final IUserQuestionProgressRepository progressRepository;
    private final IPracticeAnswerRecordRepository answerRecordRepository;

    @Override
    @Transactional
    public CreatePracticeSessionResponse createSession(CreatePracticeSessionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request cannot be null");
        }
        if (request.getNoteNodeId() == null) {
            throw new IllegalArgumentException("request.noteNodeId cannot be null");
        }
        Long userId = PracticeUserUtils.resolveUserId(request.getUserId());
        int size = request.getSize() == null ? 20 : request.getSize();
        if (size < 1) {
            throw new IllegalArgumentException("request.size cannot be less than 1");
        }
        PracticeMode mode = request.getMode() == null ? PracticeMode.SEQUENTIAL : request.getMode();

        List<Long> questionIds = resolveQuestionIds(request.getNoteNodeId(), userId, mode, size);

        PracticeSession session = new PracticeSession();
        session.setUserId(userId);
        session.setNoteNodeId(request.getNoteNodeId());
        session.setMode(mode);
        session.setStatus(PracticeSessionStatus.RUNNING);
        session.setStartedAt(LocalDateTime.now());
        session.setTotalCount(questionIds.size());
        PracticeSession savedSession = sessionRepository.save(session);

        List<PracticeSessionItem> items = new ArrayList<>();
        for (int i = 0; i < questionIds.size(); i++) {
            PracticeSessionItem item = new PracticeSessionItem();
            item.setSessionId(savedSession.getId());
            item.setQuestionId(questionIds.get(i));
            item.setSort(i);
            items.add(item);
        }
        sessionItemRepository.saveAll(items);

        CreatePracticeSessionResponse resp = new CreatePracticeSessionResponse();
        resp.setSession(toSessionVO(savedSession));
        if (!questionIds.isEmpty()) {
            Question q = questionRepository.findById(questionIds.get(0))
                    .orElseThrow(() -> new IllegalArgumentException("Question not found, id=" + questionIds.get(0)));
            resp.setQuestion(toPracticeQuestionVO(q, null, null));
        }
        return resp;
    }

    @Override
    @Transactional
    public GetPracticeItemResponse getItem(Long sessionId, Integer index, Long userId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("sessionId cannot be null");
        }
        if (index == null || index < 0) {
            throw new IllegalArgumentException("index cannot be less than 0");
        }
        Long resolvedUserId = PracticeUserUtils.resolveUserId(userId);

        PracticeSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found, id=" + sessionId));
        if (!Objects.equals(session.getUserId(), resolvedUserId)) {
            throw new IllegalArgumentException("Session does not belong to user, sessionId=" + sessionId);
        }

        PracticeSessionItem item = sessionItemRepository.findBySessionIdAndSort(sessionId, index)
                .orElseThrow(() -> new IllegalArgumentException("Session item not found, sessionId=" + sessionId + ", index=" + index));

        Question q = questionRepository.findById(item.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found, id=" + item.getQuestionId()));

        JsonNode userAnswer = null;
        if (Boolean.TRUE.equals(item.getAnswered())) {
            userAnswer = answerRecordRepository.findTopByUserIdAndSessionIdAndQuestionIdOrderByAnsweredAtDesc(resolvedUserId, sessionId, item.getQuestionId())
                    .map(PracticeAnswerRecord::getUserAnswer)
                    .orElse(null);
        }

        GetPracticeItemResponse resp = new GetPracticeItemResponse();
        resp.setSession(toSessionVO(session));
        resp.setQuestion(toPracticeQuestionVO(q, userAnswer, item.getResult()));
        return resp;
    }

    @Override
    @Transactional
    public SubmitPracticeAnswerResponse submit(Long sessionId, SubmitPracticeAnswerRequest request) {
        if (sessionId == null) {
            throw new IllegalArgumentException("sessionId cannot be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("request cannot be null");
        }
        if (request.getIndex() == null || request.getIndex() < 0) {
            throw new IllegalArgumentException("request.index cannot be less than 0");
        }
        if (request.getQuestionId() == null) {
            throw new IllegalArgumentException("request.questionId cannot be null");
        }
        if (request.getClientRequestId() == null || request.getClientRequestId().isBlank()) {
            throw new IllegalArgumentException("request.clientRequestId cannot be blank");
        }
        Long userId = PracticeUserUtils.resolveUserId(request.getUserId());

        Optional<PracticeAnswerRecord> existed = answerRecordRepository.findByUserIdAndClientRequestId(userId, request.getClientRequestId());
        if (existed.isPresent()) {
            PracticeAnswerRecord record = existed.get();
            return buildSubmitResponseFromRecord(sessionId, userId, record);
        }

        PracticeSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found, id=" + sessionId));
        if (!Objects.equals(session.getUserId(), userId)) {
            throw new IllegalArgumentException("Session does not belong to user, sessionId=" + sessionId);
        }

        PracticeSessionItem item = sessionItemRepository.findBySessionIdAndSort(sessionId, request.getIndex())
                .orElseThrow(() -> new IllegalArgumentException("Session item not found, sessionId=" + sessionId + ", index=" + request.getIndex()));
        if (!Objects.equals(item.getQuestionId(), request.getQuestionId())) {
            throw new IllegalArgumentException("Question mismatch, sessionId=" + sessionId + ", index=" + request.getIndex());
        }
        if (Boolean.TRUE.equals(item.getAnswered())) {
            PracticeAnswerRecord record = answerRecordRepository
                    .findTopByUserIdAndSessionIdAndQuestionIdOrderByAnsweredAtDesc(userId, sessionId, request.getQuestionId())
                    .orElseThrow(() -> new IllegalArgumentException("Answer record not found, sessionId=" + sessionId));
            return buildSubmitResponseFromRecord(sessionId, userId, record);
        }

        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found, id=" + request.getQuestionId()));

        PracticeAnswerResult result = judge(question, request.getUserAnswer());

        PracticeAnswerRecord record = new PracticeAnswerRecord();
        record.setUserId(userId);
        record.setQuestionId(request.getQuestionId());
        record.setNoteNodeId(request.getNoteNodeId());
        record.setSessionId(sessionId);
        record.setClientRequestId(request.getClientRequestId());
        record.setUserAnswer(request.getUserAnswer());
        record.setResult(result);
        record.setCostMs(request.getCostMs());
        record.setAnsweredAt(LocalDateTime.now());
        PracticeAnswerRecord savedRecord = answerRecordRepository.save(record);

        item.setAnswered(true);
        item.setResult(result);
        item.setAnsweredAt(savedRecord.getAnsweredAt());
        sessionItemRepository.save(item);

        updateUserProgress(userId, question.getId(), result, savedRecord.getAnsweredAt());
        updateSessionStats(session, result, request.getIndex());

        SubmitPracticeAnswerResponse resp = buildSubmitResponse(session, question, result, request.getUserAnswer());
        if (!Boolean.TRUE.equals(resp.getSession().getFinished())) {
            resp.setNextQuestion(resolveNextQuestion(session));
        }
        return resp;
    }

    @Override
    @Transactional
    public NoteNodePracticeStatsVO stats(Long noteNodeId, Long userId) {
        if (noteNodeId == null) {
            throw new IllegalArgumentException("noteNodeId cannot be null");
        }
        Long resolvedUserId = PracticeUserUtils.resolveUserId(userId);

        List<Long> questionIds = relRepository.findByNoteNodeIdOrderByQuestionIdAsc(noteNodeId).stream()
                .map(QuestionNoteNodeRel::getQuestionId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Set<Long> questionIdSet = new HashSet<>(questionIds);

        List<PracticeAnswerRecord> records = answerRecordRepository.findByUserIdAndNoteNodeId(resolvedUserId, noteNodeId);
        Set<Long> answeredQuestionIds = records.stream()
                .map(PracticeAnswerRecord::getQuestionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        long wrongCount = progressRepository.findByUserIdAndStatus(resolvedUserId, UserQuestionProgressStatus.WRONG).stream()
                .map(UserQuestionProgress::getQuestionId)
                .filter(Objects::nonNull)
                .filter(questionIdSet::contains)
                .count();
        long masteredCount = progressRepository.findByUserIdAndStatus(resolvedUserId, UserQuestionProgressStatus.MASTERED).stream()
                .map(UserQuestionProgress::getQuestionId)
                .filter(Objects::nonNull)
                .filter(questionIdSet::contains)
                .count();

        LocalDateTime lastAnsweredAt = records.stream()
                .map(PracticeAnswerRecord::getAnsweredAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        NoteNodePracticeStatsVO vo = new NoteNodePracticeStatsVO();
        vo.setNoteNodeId(noteNodeId);
        vo.setTotalQuestionCount((long) questionIds.size());
        vo.setAnsweredCount((long) answeredQuestionIds.size());
        vo.setWrongCount(wrongCount);
        vo.setMasteredCount(masteredCount);
        vo.setLastAnsweredAt(lastAnsweredAt);
        return vo;
    }

    @Override
    @Transactional
    public PageResultVO<PracticeQuestionVO> wrongQuestions(Long noteNodeId, Integer page, Integer size, Long userId) {
        Long resolvedUserId = PracticeUserUtils.resolveUserId(userId);
        int currentPage = page == null ? 1 : page;
        int pageSize = size == null ? 10 : size;
        if (currentPage < 1) {
            throw new IllegalArgumentException("page cannot be less than 1");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("size cannot be less than 1");
        }

        List<UserQuestionProgress> wrongProgresses = progressRepository.findByUserIdAndStatus(resolvedUserId, UserQuestionProgressStatus.WRONG);
        List<Long> wrongQuestionIds = wrongProgresses.stream()
                .map(UserQuestionProgress::getQuestionId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (noteNodeId != null) {
            Set<Long> nodeQuestionIds = relRepository.findByNoteNodeIdOrderByQuestionIdAsc(noteNodeId).stream()
                    .map(QuestionNoteNodeRel::getQuestionId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            wrongQuestionIds = wrongQuestionIds.stream().filter(nodeQuestionIds::contains).collect(Collectors.toList());
        }

        long total = wrongQuestionIds.size();
        int from = Math.min((currentPage - 1) * pageSize, wrongQuestionIds.size());
        int to = Math.min(from + pageSize, wrongQuestionIds.size());
        List<Long> pageIds = wrongQuestionIds.subList(from, to);
        List<Question> questions = pageIds.isEmpty() ? new ArrayList<>() : questionRepository.findAllById(pageIds);
        Map<Long, Question> map = new LinkedHashMap<>();
        for (Question q : questions) {
            if (q != null && q.getId() != null) {
                map.put(q.getId(), q);
            }
        }
        List<PracticeQuestionVO> list = pageIds.stream()
                .map(map::get)
                .filter(Objects::nonNull)
                .map(q -> toPracticeQuestionVO(q, null, null))
                .collect(Collectors.toList());

        PageInfo pageInfo = new PageInfo(total, pageSize, currentPage);
        return PageResultVO.ok(list, pageInfo);
    }

    private SubmitPracticeAnswerResponse buildSubmitResponseFromRecord(Long sessionId, Long userId, PracticeAnswerRecord record) {
        PracticeSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found, id=" + sessionId));
        if (!Objects.equals(session.getUserId(), userId)) {
            throw new IllegalArgumentException("Session does not belong to user, sessionId=" + sessionId);
        }
        Question question = questionRepository.findById(record.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found, id=" + record.getQuestionId()));
        SubmitPracticeAnswerResponse resp = buildSubmitResponse(session, question, record.getResult(), record.getUserAnswer());
        if (!Boolean.TRUE.equals(resp.getSession().getFinished())) {
            resp.setNextQuestion(resolveNextQuestion(session));
        }
        return resp;
    }

    private SubmitPracticeAnswerResponse buildSubmitResponse(PracticeSession session, Question question, PracticeAnswerResult result, JsonNode userAnswer) {
        SubmitPracticeAnswerResponse resp = new SubmitPracticeAnswerResponse();
        resp.setResult(result);
        resp.setUserAnswer(userAnswer);

        QuestionMetaInfo metaInfo = question.getMetaInfo() == null ? new QuestionMetaInfo() : question.getMetaInfo();
        resp.setCorrectAnswer(metaInfo.getAnswerKey());
        resp.setAnalysis(metaInfo.getAnalysis());
        resp.setSession(toSessionVO(session));
        return resp;
    }

    private void updateSessionStats(PracticeSession session, PracticeAnswerResult result, int answeredIndex) {
        session.setAnsweredCount(session.getAnsweredCount() + 1);
        if (result == PracticeAnswerResult.CORRECT) {
            session.setCorrectCount(session.getCorrectCount() + 1);
        } else if (result == PracticeAnswerResult.WRONG) {
            session.setWrongCount(session.getWrongCount() + 1);
        }

        int nextIndex = Math.max(session.getCurrentIndex() == null ? 0 : session.getCurrentIndex(), answeredIndex + 1);
        session.setCurrentIndex(Math.min(nextIndex, Math.max(session.getTotalCount() - 1, 0)));

        if (Objects.equals(session.getAnsweredCount(), session.getTotalCount())) {
            session.setStatus(PracticeSessionStatus.FINISHED);
            session.setFinishedAt(LocalDateTime.now());
        }
        sessionRepository.save(session);
    }

    private void updateUserProgress(Long userId, Long questionId, PracticeAnswerResult result, LocalDateTime answeredAt) {
        UserQuestionProgress progress = progressRepository.findByUserIdAndQuestionId(userId, questionId)
                .orElseGet(UserQuestionProgress::new);
        progress.setUserId(userId);
        progress.setQuestionId(questionId);
        progress.setLastAnsweredAt(answeredAt);

        if (result == PracticeAnswerResult.WRONG) {
            progress.setWrongCount(progress.getWrongCount() + 1);
            progress.setCorrectStreak(0);
            progress.setStatus(UserQuestionProgressStatus.WRONG);
            progress.setLastWrongAt(answeredAt);
        } else if (result == PracticeAnswerResult.CORRECT) {
            progress.setCorrectCount(progress.getCorrectCount() + 1);
            progress.setCorrectStreak(progress.getCorrectStreak() + 1);
            progress.setLastCorrectAt(answeredAt);
            if (progress.getCorrectStreak() >= DEFAULT_MASTERED_STREAK) {
                progress.setStatus(UserQuestionProgressStatus.MASTERED);
            } else {
                progress.setStatus(UserQuestionProgressStatus.CORRECT);
            }
        }
        progressRepository.save(progress);
    }

    private PracticeQuestionVO resolveNextQuestion(PracticeSession session) {
        if (session.getTotalCount() == null || session.getTotalCount() <= 0) {
            return null;
        }
        Integer index = session.getCurrentIndex() == null ? 0 : session.getCurrentIndex();
        if (index < 0) {
            index = 0;
        }
        PracticeSessionItem nextItem = sessionItemRepository.findBySessionIdAndSort(session.getId(), index)
                .orElse(null);
        if (nextItem == null) {
            return null;
        }
        Question q = questionRepository.findById(nextItem.getQuestionId())
                .orElse(null);
        if (q == null) {
            return null;
        }

        JsonNode userAnswer = null;
        if (Boolean.TRUE.equals(nextItem.getAnswered())) {
            userAnswer = answerRecordRepository.findTopByUserIdAndSessionIdAndQuestionIdOrderByAnsweredAtDesc(session.getUserId(), session.getId(), nextItem.getQuestionId())
                    .map(PracticeAnswerRecord::getUserAnswer)
                    .orElse(null);
        }
        return toPracticeQuestionVO(q, userAnswer, nextItem.getResult());
    }

    private PracticeAnswerResult judge(Question question, JsonNode userAnswer) {
        if (question == null) {
            return PracticeAnswerResult.WRONG;
        }
        if (userAnswer == null || userAnswer.isNull()) {
            return PracticeAnswerResult.WRONG;
        }

        QuestionMetaInfo metaInfo = question.getMetaInfo() == null ? new QuestionMetaInfo() : question.getMetaInfo();
        Map<String, Object> answerKey = metaInfo.getAnswerKey();
        if (answerKey == null) {
            return PracticeAnswerResult.WRONG;
        }

        if (question.getQuestionType() == QuestionType.SINGLE_CHOICE || question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
            var expected = readStringSet(answerKey.get("optionKeys"));
            var actual = readStringSet(userAnswer.get("optionKeys"));
            return Objects.equals(expected, actual) ? PracticeAnswerResult.CORRECT : PracticeAnswerResult.WRONG;
        }
        if (question.getQuestionType() == QuestionType.TRUE_FALSE) {
            Boolean expected = readBoolean(answerKey.get("value"));
            Boolean actual = userAnswer.has("value") && userAnswer.get("value").isBoolean() ? userAnswer.get("value").asBoolean() : null;
            return Objects.equals(expected, actual) ? PracticeAnswerResult.CORRECT : PracticeAnswerResult.WRONG;
        }
        if (question.getQuestionType() == QuestionType.FILL_BLANK) {
            Object blanksObj = answerKey.get("blanks");
            JsonNode blanksNode = userAnswer.get("blanks");
            if (!(blanksObj instanceof Map<?, ?>) || blanksNode == null || !blanksNode.isObject()) {
                return PracticeAnswerResult.WRONG;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> expectedMap = (Map<String, Object>) blanksObj;
            for (Map.Entry<String, Object> entry : expectedMap.entrySet()) {
                String blankKey = entry.getKey();
                Set<String> accept = readStringSet(entry.getValue());
                JsonNode actualNode = blanksNode.get(blankKey);
                String actual = actualNode == null || actualNode.isNull() ? null : actualNode.asText();
                if (actual == null) {
                    return PracticeAnswerResult.WRONG;
                }
                String trimmed = actual.trim();
                if (!accept.contains(trimmed)) {
                    return PracticeAnswerResult.WRONG;
                }
            }
            return PracticeAnswerResult.CORRECT;
        }
        if (question.getQuestionType() == QuestionType.SHORT_ANSWER) {
            Set<String> accept = readStringSet(answerKey.get("texts"));
            String actual = userAnswer.has("text") ? userAnswer.get("text").asText() : null;
            if (actual == null) {
                return PracticeAnswerResult.WRONG;
            }
            return accept.contains(actual.trim()) ? PracticeAnswerResult.CORRECT : PracticeAnswerResult.WRONG;
        }
        return PracticeAnswerResult.WRONG;
    }

    private Boolean readBoolean(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof String s) {
            if ("true".equalsIgnoreCase(s)) {
                return Boolean.TRUE;
            }
            if ("false".equalsIgnoreCase(s)) {
                return Boolean.FALSE;
            }
        }
        return null;
    }

    private Set<String> readStringSet(Object value) {
        if (value == null) {
            return new LinkedHashSet<>();
        }
        if (value instanceof List<?> list) {
            return list.stream()
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        if (value instanceof String s) {
            Set<String> set = new LinkedHashSet<>();
            set.add(s);
            return set;
        }
        return new LinkedHashSet<>();
    }

    private Set<String> readStringSet(JsonNode node) {
        if (node == null || node.isNull()) {
            return new LinkedHashSet<>();
        }
        if (node.isArray()) {
            Set<String> set = new LinkedHashSet<>();
            node.forEach(n -> {
                if (n != null && !n.isNull()) {
                    set.add(n.asText());
                }
            });
            return set;
        }
        if (node.isTextual()) {
            Set<String> set = new LinkedHashSet<>();
            set.add(node.asText());
            return set;
        }
        return new LinkedHashSet<>();
    }

    private List<Long> resolveQuestionIds(Long noteNodeId, Long userId, PracticeMode mode, int size) {
        List<QuestionNoteNodeRel> rels = relRepository.findByNoteNodeIdOrderByQuestionIdAsc(noteNodeId);
        List<Long> allIds = rels.stream()
                .map(QuestionNoteNodeRel::getQuestionId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(allIds)) {
            return new ArrayList<>();
        }

        List<Long> candidates = new ArrayList<>(allIds);
        if (mode == PracticeMode.RANDOM) {
            Collections.shuffle(candidates);
        } else if (mode == PracticeMode.WRONG_ONLY) {
            var wrongIds = progressRepository.findByUserIdAndStatus(userId, UserQuestionProgressStatus.WRONG).stream()
                    .map(UserQuestionProgress::getQuestionId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            List<Long> filtered = candidates.stream().filter(wrongIds::contains).collect(Collectors.toList());
            if (!filtered.isEmpty()) {
                candidates = filtered;
            }
        } else if (mode == PracticeMode.UNANSWERED_ONLY) {
            var answered = answerRecordRepository.findByUserId(userId).stream()
                    .map(PracticeAnswerRecord::getQuestionId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            List<Long> filtered = candidates.stream().filter(id -> !answered.contains(id)).collect(Collectors.toList());
            if (!filtered.isEmpty()) {
                candidates = filtered;
            }
        }

        if (candidates.size() <= size) {
            return candidates;
        }
        return new ArrayList<>(candidates.subList(0, size));
    }

    private PracticeSessionVO toSessionVO(PracticeSession session) {
        PracticeSessionVO vo = new PracticeSessionVO();
        vo.setSessionId(session.getId());
        vo.setTotalCount(session.getTotalCount());
        vo.setCurrentIndex(session.getCurrentIndex());
        vo.setAnsweredCount(session.getAnsweredCount());
        vo.setCorrectCount(session.getCorrectCount());
        vo.setWrongCount(session.getWrongCount());
        vo.setFinished(session.getStatus() == PracticeSessionStatus.FINISHED);
        return vo;
    }

    private PracticeQuestionVO toPracticeQuestionVO(Question question, JsonNode userAnswer, PracticeAnswerResult result) {
        PracticeQuestionVO vo = new PracticeQuestionVO();
        vo.setQuestionId(question.getId());
        vo.setQuestionType(question.getQuestionType());
        vo.setTitle(question.getTitle());
        vo.setStem(question.getStem());
        vo.setMetaInfo(sanitizeMetaInfo(question.getMetaInfo()));
        vo.setUserAnswer(userAnswer);
        vo.setResult(result);
        return vo;
    }

    private QuestionMetaInfo sanitizeMetaInfo(QuestionMetaInfo source) {
        QuestionMetaInfo metaInfo = source == null ? new QuestionMetaInfo() : source;

        QuestionMetaInfo target = new QuestionMetaInfo();
        if (metaInfo.getDisplay() != null) {
            QuestionMetaInfo.Display d = new QuestionMetaInfo.Display();
            d.setTypeLabel(metaInfo.getDisplay().getTypeLabel());
            d.setSection(metaInfo.getDisplay().getSection());
            d.setScore(metaInfo.getDisplay().getScore());
            target.setDisplay(d);
        }
        target.setOptions(metaInfo.getOptions() == null ? new ArrayList<>() : metaInfo.getOptions());
        target.setBlanks(metaInfo.getBlanks() == null ? new ArrayList<>() : metaInfo.getBlanks());
        target.setAssets(metaInfo.getAssets() == null ? new ArrayList<>() : metaInfo.getAssets());
        target.setExt(metaInfo.getExt() == null ? new LinkedHashMap<>() : metaInfo.getExt());
        target.setAnswerKey(new LinkedHashMap<>());
        target.setAnalysis(new LinkedHashMap<>());
        return target;
    }
}
