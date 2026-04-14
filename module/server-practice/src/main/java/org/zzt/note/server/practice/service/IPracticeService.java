package org.zzt.note.server.practice.service;

import org.athena.framework.data.jdbc.vo.PageResultVO;
import org.zzt.note.server.practice.req.CreatePracticeSessionRequest;
import org.zzt.note.server.practice.req.SubmitPracticeAnswerRequest;
import org.zzt.note.server.practice.vo.CreatePracticeSessionResponse;
import org.zzt.note.server.practice.vo.GetPracticeItemResponse;
import org.zzt.note.server.practice.vo.NoteNodePracticeStatsVO;
import org.zzt.note.server.practice.vo.PracticeQuestionVO;
import org.zzt.note.server.practice.vo.SubmitPracticeAnswerResponse;

/**
 * 做题练习服务
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
public interface IPracticeService {

    CreatePracticeSessionResponse createSession(CreatePracticeSessionRequest request);

    GetPracticeItemResponse getItem(Long sessionId, Integer index, Long userId);

    SubmitPracticeAnswerResponse submit(Long sessionId, SubmitPracticeAnswerRequest request);

    NoteNodePracticeStatsVO stats(Long noteNodeId, Long userId);

    PageResultVO<PracticeQuestionVO> wrongQuestions(Long noteNodeId, Integer page, Integer size, Long userId);
}
