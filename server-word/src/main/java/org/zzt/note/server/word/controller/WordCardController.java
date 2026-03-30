package org.zzt.note.server.word.controller;

import org.athena.framework.data.jdbc.req.BaseRequest;
import org.athena.framework.data.jdbc.web.BaseControllerV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zzt.note.server.word.entity.WordCard;
import org.zzt.note.server.word.entity.dto.WordCardDTO;
import org.zzt.note.server.word.service.IWordCardService;

/**
 * 单词卡片 CRUD 控制器
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
@RestController
@RequestMapping("/api/v1/wordCards")
public class WordCardController extends BaseControllerV2<WordCard, WordCardDTO, BaseRequest, IWordCardService> {

    @Autowired
    private IWordCardService service;

    @Override
    protected IWordCardService service() {
        return service;
    }
}
