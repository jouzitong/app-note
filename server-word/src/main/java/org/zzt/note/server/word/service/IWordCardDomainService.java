package org.zzt.note.server.word.service;

import org.zzt.note.server.word.vo.WordCardVO;

/**
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
public interface IWordCardDomainService {

    /**
     * 新增单词卡
     *
     * @param wordCard 单词卡内容
     */
    void add(WordCardVO wordCard);

    /**
     * 按笔记节点 ID 获取单词卡片详情
     *
     * @param noteId 笔记节点 ID
     * @return 单词卡片视图对象
     */
    WordCardVO get(Long noteId, int index);

}
