package org.zzt.note.data.core.convert;

import org.athena.framework.data.jdbc.convert.IConvert;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.zzt.note.data.core.entity.NoteNode;
import org.zzt.note.data.core.entity.dto.NoteNodeDTO;

/**
 *
 * @author zhouzhitong
 * @since 2026/3/27
 */
@Mapper(componentModel = "spring")
public interface NoteNodeConvert extends IConvert<NoteNode, NoteNodeDTO> {

    @Override
    @Mapping(target = "nodeContent", ignore = true)
    NoteNode toEntity(NoteNodeDTO d);

    @Override
    @Mapping(target = "meta.node", ignore = true)
    NoteNodeDTO toDTO(NoteNode noteNode);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "meta.node", ignore = true)
    @Mapping(target = "nodeContent", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void editEntityFromDto(NoteNodeDTO dto, @MappingTarget NoteNode entity);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "meta.node", ignore = true)
    @Mapping(target = "nodeContent", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    void updateEntityFromDto(NoteNodeDTO dto, @MappingTarget NoteNode entity);

    @AfterMapping
    default void bindMetaNode(@MappingTarget NoteNode node) {
        if (node != null && node.getMeta() != null) {
            node.getMeta().setNode(node);
        }
    }

}
