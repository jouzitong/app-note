import {
  createDefaultArticle,
  normalizeArticle,
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
