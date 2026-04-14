package org.zzt.note.server.word.convert;

import org.athena.framework.data.jdbc.convert.IConvert;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.zzt.note.server.word.entity.WordCard;
import org.zzt.note.server.word.entity.dto.WordCardDTO;

/**
 * 单词卡片转换器
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
@Mapper(componentModel = "spring")
public interface WordCardConvert extends IConvert<WordCard, WordCardDTO> {

    @Override
    WordCard toEntity(WordCardDTO dto);

    @Override
    WordCardDTO toDTO(WordCard entity);

    @Override
    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void editEntityFromDto(WordCardDTO dto, @MappingTarget WordCard entity);

    @Override
    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    void updateEntityFromDto(WordCardDTO dto, @MappingTarget WordCard entity);
}
