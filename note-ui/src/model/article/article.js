export function createDefaultArticle() {
  return {
    id: "",
    noteNodeId: null,
    title: "",
    paragraphs: [],
    translation: [],
    progress: {
      favorite: false,
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
    progress: {
      favorite: Boolean(progress.favorite),
      lastReadParagraphIndex:
        Number.isInteger(lastReadParagraphIndex) && lastReadParagraphIndex >= 0
          ? lastReadParagraphIndex
          : 0,
      playbackRate:
        Number.isFinite(playbackRate) && playbackRate > 0 ? playbackRate : 1.0,
    },
  };
}
