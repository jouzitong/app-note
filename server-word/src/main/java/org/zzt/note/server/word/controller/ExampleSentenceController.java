package org.zzt.note.server.word.controller;

import org.athena.framework.data.jdbc.req.BaseRequest;
import org.athena.framework.data.jdbc.web.BaseControllerV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zzt.note.server.word.entity.ExampleSentence;
import org.zzt.note.server.word.entity.dto.ExampleSentenceDTO;
import org.zzt.note.server.word.service.IExampleSentenceService;

/**
 * 例句 CRUD 控制器
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
@RestController
@RequestMapping("/api/v1/exampleSentences")
public class ExampleSentenceController
        extends BaseControllerV2<ExampleSentence, ExampleSentenceDTO, BaseRequest, IExampleSentenceService> {

    @Autowired
    private IExampleSentenceService service;

    @Override
    protected IExampleSentenceService service() {
        return service;
    }
}
