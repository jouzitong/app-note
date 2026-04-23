import {
  createDefaultArticle,
  createDefaultArticleReader,
  normalizeArticle,
  normalizeArticleReader,
} from "@/model/article/article";

export function createDefaultArticleVm() {
  return createDefaultArticle();
}

export function mapArticleDtoToVm(source = {}) {
  return normalizeArticle(source);
}

export function mergeArticleVmWithPatch(articleVm, patchDto) {
  return normalizeArticle({
    ...(articleVm || createDefaultArticle()),
    ...(patchDto || {}),
  });
}

export function createDefaultArticleReaderVm() {
  return createDefaultArticleReader();
}

export function mapArticleReaderDtoToVm(source = {}) {
  return normalizeArticleReader(source);
}
