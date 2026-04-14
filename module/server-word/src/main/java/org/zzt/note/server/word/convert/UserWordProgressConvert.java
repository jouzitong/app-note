package org.zzt.note.server.word.convert;

import org.athena.framework.data.jdbc.convert.IConvert;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.zzt.note.server.word.entity.UserWordProgress;
import org.zzt.note.server.word.entity.WordCard;
import org.zzt.note.server.word.entity.dto.UserWordProgressDTO;
import org.zzt.note.server.word.entity.enums.UserWordProgressStatus;

/**
 * 用户学习进度转换器
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
@Mapper(componentModel = "spring")
public interface UserWordProgressConvert extends IConvert<UserWordProgress, UserWordProgressDTO> {

    @Override
    @Mapping(target = "wordCard", source = "wordCardId", qualifiedByName = "toWordCard")
    @Mapping(target = "status", source = "status", qualifiedByName = "toStatusEnum")
    UserWordProgress toEntity(UserWordProgressDTO dto);

    @Override
    @Mapping(target = "wordCardId", source = "wordCard", qualifiedByName = "toWordCardId")
    @Mapping(target = "status", source = "status", qualifiedByName = "toStatusName")
    UserWordProgressDTO toDTO(UserWordProgress entity);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "wordCard", source = "wordCardId", qualifiedByName = "toWordCard")
    @Mapping(target = "status", source = "status", qualifiedByName = "toStatusEnum")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void editEntityFromDto(UserWordProgressDTO dto, @MappingTarget UserWordProgress entity);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "wordCard", source = "wordCardId", qualifiedByName = "toWordCard")
    @Mapping(target = "status", source = "status", qualifiedByName = "toStatusEnum")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    void updateEntityFromDto(UserWordProgressDTO dto, @MappingTarget UserWordProgress entity);

    @Named("toWordCard")
    default WordCard toWordCard(Long wordCardId) {
        if (wordCardId == null) {
            return null;
        }
        WordCard wordCard = new WordCard();
        wordCard.setId(wordCardId);
        return wordCard;
    }

    @Named("toWordCardId")
    default Long toWordCardId(WordCard wordCard) {
        return wordCard == null ? null : wordCard.getId();
    }

    @Named("toStatusEnum")
    default UserWordProgressStatus toStatusEnum(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return UserWordProgressStatus.valueOf(status);
    }

    @Named("toStatusName")
    default String toStatusName(UserWordProgressStatus status) {
        return status == null ? null : status.name();
    }
}
