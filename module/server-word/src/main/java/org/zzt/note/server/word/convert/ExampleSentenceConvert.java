package org.zzt.note.server.word.convert;

import org.athena.framework.data.jdbc.convert.IConvert;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.zzt.note.server.word.entity.ExampleSentence;
import org.zzt.note.server.word.entity.dto.ExampleSentenceDTO;

/**
 * 例句转换器
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
@Mapper(componentModel = "spring")
public interface ExampleSentenceConvert extends IConvert<ExampleSentence, ExampleSentenceDTO> {

    @Override
    ExampleSentence toEntity(ExampleSentenceDTO dto);

    @Override
    ExampleSentenceDTO toDTO(ExampleSentence entity);

    @Override
    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void editEntityFromDto(ExampleSentenceDTO dto, @MappingTarget ExampleSentence entity);

    @Override
    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    void updateEntityFromDto(ExampleSentenceDTO dto, @MappingTarget ExampleSentence entity);
}
