package org.zzt.note.server.word.service;

import org.athena.framework.data.jdbc.vo.PageResultVO;
import org.zzt.note.server.word.req.WordCardDomainPageRequest;
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
    default WordCardVO get(Long noteId, int index) {
        return get(noteId, index, null);
    }

    /**
     * 按笔记节点 ID 和下标获取单词卡片详情（可携带用户进度）
     *
     * @param noteId 笔记节点 ID
     * @param index  卡片下标（从0开始）
     * @param userId 用户ID（可空）
     * @return 单词卡片视图对象
     */
    WordCardVO get(Long noteId, int index, Long userId);

    /**
     * 分页查询节点下的单词卡片
     *
     * @param request 分页请求
     * @return 分页结果
     */
    PageResultVO<WordCardVO> page(WordCardDomainPageRequest request);

    /**
     * 确认当前用户已完成单词卡
     *
     * @param cardId 卡片业务ID
     * @param userId 用户ID
     * @return 更新后的单词卡片视图对象
     */
    WordCardVO confirm(String cardId, Long userId);

    /**
     * 按卡片业务ID删除单词卡（会删除关联关系，不删除例句实体）
     *
     * @param cardId 卡片业务ID
     */
    void delete(String cardId);

}
