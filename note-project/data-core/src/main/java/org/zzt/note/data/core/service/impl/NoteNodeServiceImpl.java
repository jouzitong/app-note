package org.zzt.note.data.core.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.athena.framework.data.jdbc.convert.IConvert;
import org.athena.framework.data.jpa.repository.BaseRepository;
import org.athena.framework.data.jpa.service.BaseMapperServiceV2;
import org.springframework.stereotype.Service;
import org.zzt.note.data.core.convert.NoteNodeConvert;
import org.zzt.note.data.core.entity.NoteNode;
import org.zzt.note.data.core.entity.dto.NoteNodeDTO;
import org.zzt.note.data.core.repository.INoteNodeRepository;
import org.zzt.note.data.core.service.INoteNodeService;

/**
 *
 * @author zhouzhitong
 * @since 2026/3/27
 */
@Slf4j
@AllArgsConstructor
@Service
public class NoteNodeServiceImpl
        extends BaseMapperServiceV2<NoteNode, NoteNodeDTO>
        implements INoteNodeService {

    private final INoteNodeRepository repository;

    private final NoteNodeConvert convert;

    @Override
    protected BaseRepository<NoteNode> repository() {
        return repository;
    }

    @Override
    protected IConvert<NoteNode, NoteNodeDTO> convert() {
        return convert;
    }

    @Override
    public NoteNodeDTO newDTO() {
        return new NoteNodeDTO();
    }

    @Override
    public NoteNode newEntity() {
        return new NoteNode();
    }
}
