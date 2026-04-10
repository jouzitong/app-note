package org.zzt.note.server.practice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zzt.note.server.practice.entity.Question;

import java.util.Optional;

/**
 * 题目仓储
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
public interface IQuestionRepository extends JpaRepository<Question, Long> {

    Optional<Question> findByQuestionCode(String questionCode);
}

