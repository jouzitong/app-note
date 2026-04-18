package org.zzt.note.server.word.controller;

import org.athena.framework.data.jdbc.req.BaseRequest;
import org.athena.framework.data.jdbc.web.BaseControllerV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zzt.note.server.word.entity.UserWordProgress;
import org.zzt.note.server.word.entity.dto.UserWordProgressDTO;
import org.zzt.note.server.word.service.IUserWordProgressService;

/**
 * 用户单词学习进度 CRUD 控制器
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
@RestController
@RequestMapping("/api/v1/userWordProgresses")
public class UserWordProgressController
        extends BaseControllerV2<UserWordProgress, UserWordProgressDTO, BaseRequest, IUserWordProgressService> {

    @Autowired
    private IUserWordProgressService service;

    @Override
    protected IUserWordProgressService service() {
        return service;
    }
}
