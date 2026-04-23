export function createDefaultArticle() {
  return {
    id: "",
    noteNodeId: null,
    title: "",
    paragraphs: [],
    translation: [],
    knowledge: {
      coreVocabulary: [],
      coreSentencePatterns: [],
    },
    progress: {
      favorite: false,
      completed: false,
      lastReadParagraphIndex: 0,
      playbackRate: 1.0,
    },
  };
}

export function normalizeArticle(source) {
  const fallback = createDefaultArticle();
  if (!source || typeof source !== "object") {
    return fallback;
  }
  const parsedNoteNodeId = Number(source.noteNodeId);

  const paragraphs = Array.isArray(source.paragraphs)
    ? source.paragraphs.map((paragraph) => {
        if (!Array.isArray(paragraph)) {
          return [];
        }
        return paragraph
          .filter((token) => token && typeof token === "object")
          .map((token) => ({
            text: token.text || "",
            kana: token.kana || "",
          }));
      })
    : [];

  const translation = Array.isArray(source.translation)
    ? source.translation.map((line) => `${line || ""}`)
    : [];

  const knowledge = source.knowledge || {};
  const coreVocabulary = Array.isArray(knowledge.coreVocabulary)
    ? knowledge.coreVocabulary
        .filter((item) => item && typeof item === "object")
        .map((item) => ({
          jp: item.jp || "",
          kana: item.kana || "",
          meaning: item.meaning || "",
        }))
    : [];
  const coreSentencePatterns = Array.isArray(knowledge.coreSentencePatterns)
    ? knowledge.coreSentencePatterns
        .filter((item) => item && typeof item === "object")
        .map((item) => ({
          jp: item.jp || "",
          meaning: item.meaning || "",
        }))
    : [];

  const progress = source.progress || {};
  const lastReadParagraphIndex = Number(progress.lastReadParagraphIndex);
  const playbackRate = Number(progress.playbackRate);

  return {
    id: source.id || "",
    noteNodeId:
      Number.isInteger(parsedNoteNodeId) && parsedNoteNodeId > 0
        ? parsedNoteNodeId
        : null,
    title: source.title || "",
    paragraphs,
    translation,
    knowledge: {
      coreVocabulary,
      coreSentencePatterns,
    },
    progress: {
      favorite: Boolean(progress.favorite),
      completed: Boolean(progress.completed),
      lastReadParagraphIndex:
        Number.isInteger(lastReadParagraphIndex) && lastReadParagraphIndex >= 0
          ? lastReadParagraphIndex
          : 0,
      playbackRate:
        Number.isFinite(playbackRate) && playbackRate > 0 ? playbackRate : 1.0,
    },
  };
}

export function createDefaultArticleReader() {
  return {
    noteNodeId: null,
    currentArticleIndex: 0,
    articles: [],
  };
}

export function normalizeArticleReader(source) {
  const fallback = createDefaultArticleReader();
  if (!source || typeof source !== "object") {
    return fallback;
  }

  const parsedNoteNodeId = Number(source.noteNodeId);
  const articles = Array.isArray(source.articles)
    ? source.articles.map((item) => normalizeArticle(item))
    : [];
  const parsedIndex = Number(source.currentArticleIndex);
  const maxIndex = Math.max(0, articles.length - 1);
  const currentArticleIndex =
    Number.isInteger(parsedIndex) && parsedIndex >= 0
      ? Math.min(parsedIndex, maxIndex)
      : 0;

  return {
    noteNodeId:
      Number.isInteger(parsedNoteNodeId) && parsedNoteNodeId > 0
        ? parsedNoteNodeId
        : null,
    currentArticleIndex,
    articles,
  };
}
