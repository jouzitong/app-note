package org.zzt.note.server.word.service;

import org.athena.framework.data.jdbc.serivce.IMapperServiceV2;
import org.zzt.note.server.word.entity.UserWordProgress;
import org.zzt.note.server.word.entity.dto.UserWordProgressDTO;

/**
 * 用户单词学习进度服务
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
public interface IUserWordProgressService extends IMapperServiceV2<UserWordProgress, UserWordProgressDTO> {
}
