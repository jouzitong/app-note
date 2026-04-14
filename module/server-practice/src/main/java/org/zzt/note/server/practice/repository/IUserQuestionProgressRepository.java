package org.zzt.note.server.practice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zzt.note.server.practice.entity.UserQuestionProgress;
import org.zzt.note.server.practice.entity.enums.UserQuestionProgressStatus;

import java.util.List;
import java.util.Optional;

/**
 * 用户题目进度仓储
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
public interface IUserQuestionProgressRepository extends JpaRepository<UserQuestionProgress, Long> {

    Optional<UserQuestionProgress> findByUserIdAndQuestionId(Long userId, Long questionId);

    List<UserQuestionProgress> findByUserIdAndStatus(Long userId, UserQuestionProgressStatus status);
}

