package org.zzt.note.server.word.service;

import org.athena.framework.data.jdbc.serivce.IMapperServiceV2;
import org.zzt.note.server.word.entity.WordCard;
import org.zzt.note.server.word.entity.dto.WordCardDTO;

/**
 * 单词卡片服务
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
public interface IWordCardService extends IMapperServiceV2<WordCard, WordCardDTO> {
}
