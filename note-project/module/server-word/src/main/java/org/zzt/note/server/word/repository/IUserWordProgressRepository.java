package org.zzt.note.server.word.repository;

import org.athena.framework.data.jpa.repository.BaseRepository;
import org.zzt.note.server.word.entity.UserWordProgress;

import java.util.List;
import java.util.Optional;

/**
 * 用户单词学习进度仓储
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
public interface IUserWordProgressRepository extends BaseRepository<UserWordProgress> {

    Optional<UserWordProgress> findByUserIdAndWordCard_Id(Long userId, Long wordCardId);

    List<UserWordProgress> findByUserId(Long userId);

    List<UserWordProgress> findByUserIdAndWordCard_IdIn(Long userId, List<Long> wordCardIds);
}
