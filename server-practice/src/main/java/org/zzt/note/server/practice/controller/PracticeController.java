package org.zzt.note.server.practice.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.athena.framework.data.jdbc.vo.PageResultVO;
import org.zzt.note.server.practice.req.CreatePracticeSessionRequest;
import org.zzt.note.server.practice.req.SubmitPracticeAnswerRequest;
import org.zzt.note.server.practice.service.IPracticeService;
import org.zzt.note.server.practice.utils.PracticeUserUtils;
import org.zzt.note.server.practice.vo.CreatePracticeSessionResponse;
import org.zzt.note.server.practice.vo.GetPracticeItemResponse;
import org.zzt.note.server.practice.vo.NoteNodePracticeStatsVO;
import org.zzt.note.server.practice.vo.PracticeQuestionVO;
import org.zzt.note.server.practice.vo.SubmitPracticeAnswerResponse;

/**
 * 做题练习接口
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/practices")
public class PracticeController {

    private final IPracticeService practiceService;

    @PostMapping("/sessions")
    public CreatePracticeSessionResponse createSession(@RequestBody CreatePracticeSessionRequest request) {
        if (request != null) {
            request.setUserId(PracticeUserUtils.resolveUserId(request.getUserId()));
        }
        return practiceService.createSession(request);
    }

    @GetMapping("/sessions/{sessionId}/items/{index}")
    public GetPracticeItemResponse getItem(@PathVariable("sessionId") Long sessionId,
                                           @PathVariable("index") Integer index,
                                           @RequestParam(value = "userId", required = false) Long userId) {
        return practiceService.getItem(sessionId, index, PracticeUserUtils.resolveUserId(userId));
    }

    @PostMapping("/sessions/{sessionId}/submit")
    public SubmitPracticeAnswerResponse submit(@PathVariable("sessionId") Long sessionId,
                                               @RequestBody SubmitPracticeAnswerRequest request) {
        if (request != null) {
            request.setUserId(PracticeUserUtils.resolveUserId(request.getUserId()));
        }
        return practiceService.submit(sessionId, request);
    }

    @GetMapping("/stats")
    public NoteNodePracticeStatsVO stats(@RequestParam("noteNodeId") Long noteNodeId,
                                         @RequestParam(value = "userId", required = false) Long userId) {
        return practiceService.stats(noteNodeId, PracticeUserUtils.resolveUserId(userId));
    }

    @GetMapping("/wrong-questions")
    public PageResultVO<PracticeQuestionVO> wrongQuestions(@RequestParam(value = "noteNodeId", required = false) Long noteNodeId,
                                                           @RequestParam(value = "page", required = false) Integer page,
                                                           @RequestParam(value = "size", required = false) Integer size,
                                                           @RequestParam(value = "userId", required = false) Long userId) {
        return practiceService.wrongQuestions(noteNodeId, page, size, PracticeUserUtils.resolveUserId(userId));
    }
}
