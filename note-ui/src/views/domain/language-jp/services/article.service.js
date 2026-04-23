import articleApi from "@/api/articles";
import { mapArticleReaderDtoToVm } from "@/mappers/domain/language-jp/article.mapper";

export async function fetchArticleByNoteNode(noteNodeId) {
  const dto = await articleApi.getArticleByNoteNode(noteNodeId);
  return mapArticleReaderDtoToVm(dto || {});
}
