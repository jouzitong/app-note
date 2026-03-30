package org.zzt.note.server.word.repository;

import org.athena.framework.data.jpa.repository.BaseRepository;
import org.zzt.note.server.word.entity.ExampleSentence;

import java.util.List;

/**
 * 例句仓储
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
public interface IExampleSentenceRepository extends BaseRepository<ExampleSentence> {

    List<ExampleSentence> findByExampleCodeIn(List<String> exampleCodes);
}
