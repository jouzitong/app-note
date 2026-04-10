package org.zzt.note.server.practice.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.athena.framework.data.jdbc.vo.PageInfo;
import org.athena.framework.data.jdbc.vo.PageResultVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.zzt.note.server.practice.entity.Question;
import org.zzt.note.server.practice.entity.QuestionNoteNodeRel;
import org.zzt.note.server.practice.repository.IQuestionNoteNodeRelRepository;
import org.zzt.note.server.practice.repository.IQuestionRepository;
import org.zzt.note.server.practice.req.QuestionDomainPageRequest;
import org.zzt.note.server.practice.service.IQuestionDomainService;
import org.zzt.note.server.practice.vo.QuestionVO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 题目领域服务实现
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
@Service
@AllArgsConstructor
public class QuestionDomainServiceImpl implements IQuestionDomainService {

    private final IQuestionRepository questionRepository;
    private final IQuestionNoteNodeRelRepository relRepository;

    @Override
    @Transactional
    public QuestionVO upsert(QuestionVO questionVO) {
        if (questionVO == null) {
            throw new IllegalArgumentException("questionVO cannot be null");
        }
        if (questionVO.getQuestionCode() == null || questionVO.getQuestionCode().isBlank()) {
            throw new IllegalArgumentException("questionVO.questionCode cannot be blank");
        }
        if (questionVO.getTitle() == null || questionVO.getTitle().isBlank()) {
            throw new IllegalArgumentException("questionVO.title cannot be blank");
        }
        if (questionVO.getStem() == null || questionVO.getStem().isBlank()) {
            throw new IllegalArgumentException("questionVO.stem cannot be blank");
        }
        if (questionVO.getQuestionType() == null) {
            throw new IllegalArgumentException("questionVO.questionType cannot be null");
        }

        Optional<Question> existing = questionRepository.findByQuestionCode(questionVO.getQuestionCode());
        Question entity = existing.orElseGet(Question::new);
        entity.setQuestionCode(questionVO.getQuestionCode());
        entity.setTitle(questionVO.getTitle());
        entity.setStem(questionVO.getStem());
        entity.setQuestionType(questionVO.getQuestionType());
        entity.setDifficulty(questionVO.getDifficulty());
        entity.setLocale(questionVO.getLocale());
        if (questionVO.getMetaInfo() != null) {
            entity.setMetaInfo(questionVO.getMetaInfo());
        }

        Question saved = questionRepository.save(entity);

        if (!CollectionUtils.isEmpty(questionVO.getBindNoteNodeIds())) {
            relRepository.deleteByQuestionId(saved.getId());
            List<QuestionNoteNodeRel> rels = questionVO.getBindNoteNodeIds().stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(noteNodeId -> {
                        QuestionNoteNodeRel rel = new QuestionNoteNodeRel();
                        rel.setQuestionId(saved.getId());
                        rel.setNoteNodeId(noteNodeId);
                        return rel;
                    })
                    .collect(Collectors.toList());
            relRepository.saveAll(rels);
        }

        return toVO(saved);
    }

    @Override
    @Transactional
    public PageResultVO<QuestionVO> page(QuestionDomainPageRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request cannot be null");
        }
        if (request.getNoteNodeId() == null) {
            throw new IllegalArgumentException("request.noteNodeId cannot be null");
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
        Page<QuestionNoteNodeRel> relPage = relRepository.findByNoteNodeIdOrderByQuestionIdAsc(request.getNoteNodeId(), pageable);
        List<Long> questionIds = relPage.getContent().stream()
                .map(QuestionNoteNodeRel::getQuestionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(questionIds)) {
            PageInfo pageInfo = new PageInfo(relPage.getTotalElements(), size, currentPage);
            return PageResultVO.ok(new ArrayList<>(), pageInfo);
        }

        List<Question> questions = questionRepository.findAllById(questionIds);
        Map<Long, Question> questionMap = new LinkedHashMap<>();
        for (Question q : questions) {
            if (q != null && q.getId() != null) {
                questionMap.put(q.getId(), q);
            }
        }
        List<QuestionVO> list = questionIds.stream()
                .map(questionMap::get)
                .filter(Objects::nonNull)
                .map(this::toVO)
                .collect(Collectors.toList());

        PageInfo pageInfo = new PageInfo(relPage.getTotalElements(), size, currentPage);
        return PageResultVO.ok(list, pageInfo);
    }

    private QuestionVO toVO(Question entity) {
        QuestionVO vo = new QuestionVO();
        vo.setId(entity.getId());
        vo.setQuestionCode(entity.getQuestionCode());
        vo.setTitle(entity.getTitle());
        vo.setStem(entity.getStem());
        vo.setQuestionType(entity.getQuestionType());
        vo.setDifficulty(entity.getDifficulty());
        vo.setLocale(entity.getLocale());
        vo.setMetaInfo(entity.getMetaInfo());
        return vo;
    }
}

