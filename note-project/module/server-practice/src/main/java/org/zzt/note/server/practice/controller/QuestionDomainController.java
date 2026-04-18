package org.zzt.note.server.practice.controller;

import lombok.AllArgsConstructor;
import org.athena.framework.data.jdbc.vo.PageResultVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zzt.note.server.practice.req.QuestionDomainPageRequest;
import org.zzt.note.server.practice.service.IQuestionDomainService;
import org.zzt.note.server.practice.vo.QuestionVO;

/**
 * 题目领域控制器
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/questions/domain")
public class QuestionDomainController {

    private final IQuestionDomainService questionDomainService;

    @PostMapping
    public QuestionVO upsert(@RequestBody QuestionVO question) {
        return questionDomainService.upsert(question);
    }

    @GetMapping
    public PageResultVO<QuestionVO> page(QuestionDomainPageRequest request) {
        return questionDomainService.page(request);
    }
}

