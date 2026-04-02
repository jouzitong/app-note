package org.zzt.note.server.word.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.athena.framework.data.jdbc.vo.PageResultVO;
import org.zzt.note.server.word.req.WordCardDomainPageRequest;
import org.zzt.note.server.word.service.IWordCardDomainService;
import org.zzt.note.server.word.vo.WordCardVO;

/**
 * 单词卡片领域控制器
 *
 * @author zhouzhitong
 * @since 2026/3/30
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/wordCards/domain")
public class WordCardDomainController {

    private final IWordCardDomainService wordCardDomainService;

    @PostMapping
    public void add(@RequestBody WordCardVO wordCard) {
        wordCardDomainService.add(wordCard);
    }

    @GetMapping("/{noteId}/{index}")
    public WordCardVO get(@PathVariable("noteId") Long noteId,
                          @PathVariable("index") int index,
                          @RequestParam(value = "userId", required = false) Long userId) {
        return wordCardDomainService.get(noteId, index, userId);
    }

    @GetMapping
    public PageResultVO<WordCardVO> page(WordCardDomainPageRequest request) {
        return wordCardDomainService.page(request);
    }

    @DeleteMapping("/{cardId}")
    public void delete(@PathVariable("cardId") String cardId) {
        wordCardDomainService.delete(cardId);
    }
}
