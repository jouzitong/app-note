package org.zzt.note.server.word.article.controller;

import lombok.AllArgsConstructor;
import org.athena.framework.data.jdbc.vo.PageResultVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zzt.note.server.word.article.req.ArticleDomainPageRequest;
import org.zzt.note.server.word.article.service.IArticleDomainService;
import org.zzt.note.server.word.article.vo.ArticleVO;
import org.zzt.note.server.word.utils.WordUserUtils;

/**
 * 文章阅读领域控制器
 *
 * @author zhouzhitong
 * @since 2026/4/7
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/articles/domain")
public class ArticleDomainController {

    private final IArticleDomainService articleDomainService;

    @PostMapping
    public void save(@RequestBody ArticleVO article) {
        articleDomainService.save(article);
    }

    @GetMapping("/{articleId}")
    public ArticleVO get(@PathVariable("articleId") String articleId,
                         @RequestParam(value = "userId", required = false) Long userId) {
        return articleDomainService.get(articleId, WordUserUtils.resolveUserId(userId));
    }

    @GetMapping("/note-node/{noteNodeId}")
    public ArticleVO getByNoteNode(@PathVariable("noteNodeId") Long noteNodeId,
                                   @RequestParam(value = "userId", required = false) Long userId) {
        return articleDomainService.getByNoteNodeId(noteNodeId, WordUserUtils.resolveUserId(userId));
    }

    @GetMapping
    public PageResultVO<ArticleVO> page(ArticleDomainPageRequest request) {
        if (request != null) {
            request.setUserId(WordUserUtils.resolveUserId(request.getUserId()));
        }
        return articleDomainService.page(request);
    }

    @PostMapping("/{articleId}/favorite")
    public ArticleVO updateFavorite(@PathVariable("articleId") String articleId,
                                    @RequestParam("favorite") Boolean favorite,
                                    @RequestParam(value = "userId", required = false) Long userId) {
        return articleDomainService.updateFavorite(articleId, WordUserUtils.resolveUserId(userId), favorite);
    }

    @PostMapping("/{articleId}/playback-rate")
    public ArticleVO updatePlaybackRate(@PathVariable("articleId") String articleId,
                                        @RequestParam("playbackRate") Double playbackRate,
                                        @RequestParam(value = "userId", required = false) Long userId) {
        return articleDomainService.updatePlaybackRate(articleId, WordUserUtils.resolveUserId(userId), playbackRate);
    }

    @PostMapping("/{articleId}/position")
    public ArticleVO updatePosition(@PathVariable("articleId") String articleId,
                                    @RequestParam("paragraphIndex") Integer paragraphIndex,
                                    @RequestParam(value = "userId", required = false) Long userId) {
        return articleDomainService.updatePosition(articleId, WordUserUtils.resolveUserId(userId), paragraphIndex);
    }

    @DeleteMapping("/{articleId}")
    public void delete(@PathVariable("articleId") String articleId) {
        articleDomainService.delete(articleId);
    }
}
