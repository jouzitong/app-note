package org.zzt.note.server.word.article.service;

import org.athena.framework.data.jdbc.vo.PageResultVO;
import org.zzt.note.server.word.article.req.ArticleDomainPageRequest;
import org.zzt.note.server.word.article.vo.ArticleVO;

/**
 * 文章阅读领域服务
 *
 * @author zhouzhitong
 * @since 2026/4/7
 */
public interface IArticleDomainService {

    /**
     * 新增/覆盖文章（导入 JSON）
     *
     * @param article 文章数据
     */
    void save(ArticleVO article);

    /**
     * 查询文章详情（包含用户进度）
     *
     * @param articleId 文章业务 ID
     * @return 文章详情
     */
    ArticleVO get(String articleId);

    /**
     * 按笔记节点 ID 查询文章详情（包含用户进度）
     *
     * @param noteNodeId 笔记节点 ID
     * @return 文章详情
     */
    ArticleVO getByNoteNodeId(Long noteNodeId);

    /**
     * 分页查询文章列表
     *
     * @param request 分页请求
     * @return 分页结果
     */
    PageResultVO<ArticleVO> page(ArticleDomainPageRequest request);

    /**
     * 更新收藏状态
     *
     * @param articleId 文章业务 ID
     * @param favorite  收藏状态
     * @return 更新后的详情
     */
    ArticleVO updateFavorite(String articleId, Boolean favorite);

    /**
     * 更新播放速度
     *
     * @param articleId    文章业务 ID
     * @param playbackRate 播放速度
     * @return 更新后的详情
     */
    ArticleVO updatePlaybackRate(String articleId, Double playbackRate);

    /**
     * 更新阅读位置
     *
     * @param articleId      文章业务 ID
     * @param paragraphIndex 段落下标（从 0 开始）
     * @return 更新后的详情
     */
    ArticleVO updatePosition(String articleId, Integer paragraphIndex);

    /**
     * 删除文章
     *
     * @param articleId 文章业务 ID
     */
    void delete(String articleId);
}
