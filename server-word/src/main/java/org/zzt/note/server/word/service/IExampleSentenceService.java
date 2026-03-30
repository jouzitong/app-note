package org.zzt.note.server.word.service;

import org.athena.framework.data.jdbc.serivce.IMapperServiceV2;
import org.zzt.note.server.word.entity.ExampleSentence;
import org.zzt.note.server.word.entity.dto.ExampleSentenceDTO;

/**
 * 例句服务
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
public interface IExampleSentenceService extends IMapperServiceV2<ExampleSentence, ExampleSentenceDTO> {
}
