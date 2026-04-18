package org.zzt.note.server.word.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.athena.framework.data.jdbc.convert.IConvert;
import org.athena.framework.data.jpa.repository.BaseRepository;
import org.athena.framework.data.jpa.service.BaseMapperServiceV2;
import org.springframework.stereotype.Service;
import org.zzt.note.server.word.convert.ExampleSentenceConvert;
import org.zzt.note.server.word.entity.ExampleSentence;
import org.zzt.note.server.word.entity.dto.ExampleSentenceDTO;
import org.zzt.note.server.word.repository.IExampleSentenceRepository;
import org.zzt.note.server.word.service.IExampleSentenceService;

/**
 * 例句服务实现
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
@Slf4j
@AllArgsConstructor
@Service
public class ExampleSentenceServiceImpl
        extends BaseMapperServiceV2<ExampleSentence, ExampleSentenceDTO>
        implements IExampleSentenceService {

    private final IExampleSentenceRepository repository;

    private final ExampleSentenceConvert convert;

    @Override
    protected BaseRepository<ExampleSentence> repository() {
        return repository;
    }

    @Override
    protected IConvert<ExampleSentence, ExampleSentenceDTO> convert() {
        return convert;
    }

    @Override
    public ExampleSentenceDTO newDTO() {
        return new ExampleSentenceDTO();
    }

    @Override
    public ExampleSentence newEntity() {
        return new ExampleSentence();
    }
}
