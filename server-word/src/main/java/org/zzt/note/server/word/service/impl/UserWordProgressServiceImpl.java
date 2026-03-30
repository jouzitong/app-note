package org.zzt.note.server.word.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.athena.framework.data.jdbc.convert.IConvert;
import org.athena.framework.data.jpa.repository.BaseRepository;
import org.athena.framework.data.jpa.service.BaseMapperServiceV2;
import org.springframework.stereotype.Service;
import org.zzt.note.server.word.convert.UserWordProgressConvert;
import org.zzt.note.server.word.entity.UserWordProgress;
import org.zzt.note.server.word.entity.dto.UserWordProgressDTO;
import org.zzt.note.server.word.repository.IUserWordProgressRepository;
import org.zzt.note.server.word.service.IUserWordProgressService;

/**
 * 用户单词学习进度服务实现
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
@Slf4j
@AllArgsConstructor
@Service
public class UserWordProgressServiceImpl
        extends BaseMapperServiceV2<UserWordProgress, UserWordProgressDTO>
        implements IUserWordProgressService {

    private final IUserWordProgressRepository repository;

    private final UserWordProgressConvert convert;

    @Override
    protected BaseRepository<UserWordProgress> repository() {
        return repository;
    }

    @Override
    protected IConvert<UserWordProgress, UserWordProgressDTO> convert() {
        return convert;
    }

    @Override
    public UserWordProgressDTO newDTO() {
        return new UserWordProgressDTO();
    }

    @Override
    public UserWordProgress newEntity() {
        return new UserWordProgress();
    }
}
