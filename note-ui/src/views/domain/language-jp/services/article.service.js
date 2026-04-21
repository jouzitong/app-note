import articleApi from "@/api/articles";
import {
  mapArticleDtoToVm,
  mergeArticleVmWithPatch,
} from "@/mappers/domain/language-jp/article.mapper";

export async function fetchArticleByNoteNode(noteNodeId) {
  const dto = await articleApi.getArticleByNoteNode(noteNodeId);
  return mapArticleDtoToVm(dto || {});
}

export async function saveArticleFavorite(articleVm, favorite) {
  const updatedDto = await articleApi.updateArticleFavorite(
    articleVm?.id,
    favorite
  );
  return mergeArticleVmWithPatch(articleVm, updatedDto);
}

export async function saveArticlePlaybackRate(articleVm, playbackRate) {
  const updatedDto = await articleApi.updateArticlePlaybackRate(
    articleVm?.id,
    playbackRate
  );
  return mergeArticleVmWithPatch(articleVm, updatedDto);
}

export async function saveArticlePosition(articleVm, paragraphIndex) {
  const updatedDto = await articleApi.updateArticlePosition(
    articleVm?.id,
    paragraphIndex
  );
  return mergeArticleVmWithPatch(articleVm, updatedDto);
}
