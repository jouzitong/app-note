package org.zzt.note.data.core.entity.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.athena.framework.data.jpa.domain.dto.BaseDTO;

/**
 *
 * @author zhouzhitong
 * @since 2026/3/27
 */
@Getter
@Setter
@NoArgsConstructor
public class NoteTagDTO extends BaseDTO {

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 标签名称
     */
    private String label;

    /**
     * 样式类名
     */
    private String className;
}
