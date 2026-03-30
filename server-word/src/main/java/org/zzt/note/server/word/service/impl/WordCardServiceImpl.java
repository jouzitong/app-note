package org.zzt.note.server.word.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.athena.framework.data.jdbc.convert.IConvert;
import org.athena.framework.data.jpa.repository.BaseRepository;
import org.athena.framework.data.jpa.service.BaseMapperServiceV2;
import org.springframework.stereotype.Service;
import org.zzt.note.server.word.convert.WordCardConvert;
import org.zzt.note.server.word.entity.WordCard;
import org.zzt.note.server.word.entity.dto.WordCardDTO;
import org.zzt.note.server.word.repository.IWordCardRepository;
import org.zzt.note.server.word.service.IWordCardService;

/**
 * 单词卡片服务实现
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
@Slf4j
@AllArgsConstructor
@Service
public class WordCardServiceImpl
        extends BaseMapperServiceV2<WordCard, WordCardDTO>
        implements IWordCardService {

    private final IWordCardRepository repository;

    private final WordCardConvert convert;

    @Override
    protected BaseRepository<WordCard> repository() {
        return repository;
    }

    @Override
    protected IConvert<WordCard, WordCardDTO> convert() {
        return convert;
    }

    @Override
    public WordCardDTO newDTO() {
        return new WordCardDTO();
    }

    @Override
    public WordCard newEntity() {
        return new WordCard();
    }
}
