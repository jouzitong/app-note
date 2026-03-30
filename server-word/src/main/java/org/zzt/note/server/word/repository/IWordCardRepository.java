package org.zzt.note.server.word.repository;

import org.athena.framework.data.jpa.repository.BaseRepository;
import org.zzt.note.server.word.entity.WordCard;

import java.util.List;

/**
 * 单词卡片仓储
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
public interface IWordCardRepository extends BaseRepository<WordCard> {

    List<WordCard> findByCardCodeIn(List<String> cardCodes);
}
