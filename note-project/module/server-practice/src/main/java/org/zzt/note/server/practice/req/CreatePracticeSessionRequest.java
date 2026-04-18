package org.zzt.note.server.practice.req;

import lombok.Data;
import org.zzt.note.server.practice.entity.enums.PracticeMode;

/**
 * 创建练习会话请求
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
@Data
public class CreatePracticeSessionRequest {
    private Long noteNodeId;
    private PracticeMode mode = PracticeMode.SEQUENTIAL;
    private Integer size = 20;
    private Long userId;
}

