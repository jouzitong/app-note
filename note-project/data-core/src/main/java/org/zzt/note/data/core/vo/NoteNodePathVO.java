package org.zzt.note.data.core.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.zzt.note.data.core.entity.dto.NoteNodeMetaDTO;
import org.zzt.note.data.core.type.NoteType;

/**
 *
 * @author zhouzhitong
 * @since 2026/3/28
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NoteNodePathVO {

    private Long id;

    private String title;

    private NoteType noteType;

    private NoteNodeMetaDTO meta;

    public NoteNodePathVO(Long id, String title, NoteType noteType) {
        this.id = id;
        this.title = title;
        this.noteType = noteType;
    }

}
