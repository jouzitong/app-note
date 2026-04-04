/**
 * @typedef {Object} WordInfo
 * @property {string} text
 * @property {string} level
 * @property {string} audioUrl TODO(后端待实现): 单词音频资源地址。当前前端先预留字段，
 * 后续由后端返回可访问 URL（如 CDN/对象存储签名地址），返回后前端将优先使用该地址播放。
 */

/**
 * @typedef {Object} TagInfo
 * @property {string} name
 * @property {string} className
 */

/**
 * @typedef {Object} MeaningMeta
 * @property {string} kana
 * @property {string} zh
 * @property {string} romaji
 */

/**
 * @typedef {Object} MeaningSection
 * @property {boolean} collapsedByDefault
 * @property {MeaningMeta} meta
 * @property {string} description
 */

/**
 * @typedef {Object} WordGrammarBreakdownItem
 * @property {string} word
 * @property {string} desc
 */

/**
 * @typedef {Object} FixedPattern
 * @property {string} pattern
 * @property {string} meaningZh
 */

/**
 * @typedef {Object} ExampleExplain
 * @property {boolean} collapsedByDefault
 * @property {string} reading
 * @property {string} romaji
 * @property {string} meaningZh
 * @property {WordGrammarBreakdownItem[]} wordGrammarBreakdown
 * @property {FixedPattern} fixedPattern
 */

/**
 * @typedef {Object} ExampleItem
 * @property {string} id
 * @property {string} sentence
 * @property {string} audioUrl TODO(后端待实现): 例句音频资源地址。当前前端先预留字段，
 * 后续由后端返回可访问 URL，返回后前端将优先使用该地址播放。
 * @property {ExampleExplain} explain
 */

/**
 * @typedef {Object} ExamplesSection
 * @property {boolean} collapsedByDefault
 * @property {ExampleItem[]} items
 */

/**
 * @typedef {Object} VocabularyItem
 * @property {string} text
 * @property {string} kana
 */

/**
 * @typedef {Object} VocabularySection
 * @property {VocabularyItem[]} items
 */

/**
 * @typedef {Object} Sections
 * @property {MeaningSection} meaning
 * @property {ExamplesSection} examples
 * @property {VocabularySection} synonyms
 * @property {VocabularySection} related
 */

/**
 * @typedef {Object} ActionInfo
 * @property {string} key
 * @property {string} icon
 * @property {string} title
 */

/**
 * @typedef {Object} ProgressInfo
 * @property {boolean} done
 * @property {boolean} hard
 * @property {boolean} favorite
 * @property {string} status
 * @property {number} reviewCount
 * @property {number} correctCount
 * @property {number} wrongCount
 * @property {string|null} lastReviewedAt
 */

/**
 * @typedef {Object} WordCardVO
 * @property {string} id
 * @property {WordInfo} word
 * @property {boolean} done
 * @property {ProgressInfo} progress
 * @property {TagInfo[]} tags
 * @property {Sections} sections
 * @property {ActionInfo[]} actions
 */

/**
 * 与后端 WordCardVO 对齐的默认结构。
 * @returns {WordCardVO}
 */
export function createDefaultWordCard() {
  return {
    id: "",
    word: {
      text: "",
      level: "",
      // TODO(后端待实现): 由接口返回单词音频 URL，当前默认空字符串。
      audioUrl: "",
    },
    done: false,
    progress: {
      done: false,
      hard: false,
      favorite: false,
      status: "NEW",
      reviewCount: 0,
      correctCount: 0,
      wrongCount: 0,
      lastReviewedAt: null,
    },
    tags: [],
    sections: {
      meaning: {
        collapsedByDefault: true,
        meta: {
          kana: "",
          zh: "",
          romaji: "",
        },
        description: "",
      },
      examples: {
        collapsedByDefault: false,
        items: [],
      },
      synonyms: {
        items: [],
      },
      related: {
        items: [],
      },
    },
    actions: [
      { key: "done", icon: "✓", title: "完成" },
      { key: "hard", icon: "⚠", title: "易错" },
      { key: "favorite", icon: "★", title: "收藏" },
      { key: "audio", icon: "🔊", title: "整卡朗读" },
      { key: "next", icon: "⏭", title: "下一个" },
    ],
  };
}

/**
 * 页面展示用示例数据（结构与 WordCardVO 对齐）。
 * @returns {WordCardVO}
 */
export function createMockWordCard() {
  return {
    id: "jp-n5-watashi-001",
    word: {
      text: "私",
      level: "N5",
    },
    done: false,
    progress: {
      done: false,
      hard: false,
      favorite: true,
      status: "LEARNING",
      reviewCount: 2,
      correctCount: 1,
      wrongCount: 1,
      lastReviewedAt: null,
    },
    tags: [
      { name: "N5", className: "tag-n5" },
      { name: "代词", className: "tag-pos" },
      { name: "人称", className: "tag-type" },
      { name: "核心词", className: "tag-core" },
    ],
    sections: {
      meaning: {
        collapsedByDefault: true,
        meta: {
          kana: "わたし",
          zh: "我",
          romaji: "wa-ta-shi",
        },
        description: "表示“我”，常用第一人称代词，语气中性，适用于大多数场景。",
      },
      examples: {
        collapsedByDefault: false,
        items: [
          {
            id: "ex-1",
            sentence: "これは 私 の 本 です。",
            // TODO(后端待实现): 由接口返回例句音频 URL，当前默认空字符串。
            audioUrl: "",
            explain: {
              collapsedByDefault: true,
              reading: "これは わたし の ほん です",
              romaji: "kore wa watashi no hon desu",
              meaningZh: "这是我的书。",
              wordGrammarBreakdown: [
                { word: "これ", desc: "这（指示代词）" },
                { word: "は", desc: "主题助词，提示“这是在说什么”" },
                { word: "私（わたし）", desc: "我" },
                { word: "の", desc: "所属助词，表示“我的”" },
                { word: "本（ほん）", desc: "书" },
                { word: "です", desc: "礼貌判断句尾，相当于“是”" },
              ],
              fixedPattern: {
                pattern: "これは A です。",
                meaningZh: "这是 A。",
              },
            },
          },
          {
            id: "ex-2",
            sentence: "私 は 学生 です。",
            // TODO(后端待实现): 由接口返回例句音频 URL，当前默认空字符串。
            audioUrl: "",
            explain: {
              collapsedByDefault: true,
              reading: "わたし は がくせい です",
              romaji: "watashi wa gakusei desu",
              meaningZh: "我是学生。",
              wordGrammarBreakdown: [
                { word: "私（わたし）", desc: "我" },
                { word: "は", desc: "主题助词，提示主题“我”" },
                { word: "学生（がくせい）", desc: "学生" },
                { word: "です", desc: "礼貌判断句尾，相当于“是”" },
              ],
              fixedPattern: {
                pattern: "A は B です。",
                meaningZh: "A 是 B。",
              },
            },
          },
        ],
      },
      synonyms: {
        items: [
          { text: "僕", kana: "ぼく" },
          { text: "俺", kana: "おれ" },
          { text: "自分", kana: "じぶん" },
        ],
      },
      related: {
        items: [
          { text: "あなた", kana: "" },
          { text: "彼", kana: "かれ" },
          { text: "名前", kana: "なまえ" },
        ],
      },
    },
    actions: [
      { key: "done", icon: "✓", title: "完成" },
      { key: "hard", icon: "⚠", title: "易错" },
      { key: "favorite", icon: "★", title: "收藏" },
      { key: "audio", icon: "🔊", title: "整卡朗读" },
      { key: "next", icon: "⏭", title: "下一个" },
    ],
  };
}

/**
 * 将任意接口返回对象归一化为 WordCardVO。
 * @param {Partial<WordCardVO>} source
 * @returns {WordCardVO}
 */
export function normalizeWordCard(source = {}) {
  const defaults = createDefaultWordCard();
  const rawProgress = source.progress || {};
  const rawWord = source.word || {};
  const rawSections = source.sections || {};
  const rawMeaning = rawSections.meaning || {};
  const rawMeaningMeta = rawMeaning.meta || {};
  const rawExamples = rawSections.examples || {};
  const rawSynonyms = rawSections.synonyms || {};
  const rawRelated = rawSections.related || {};

  return {
    ...defaults,
    ...source,
    word: {
      ...defaults.word,
      ...rawWord,
      // TODO(后端待实现): 接口正式支持前，audioUrl 可能缺失，前端统一兜底为空字符串。
      audioUrl: rawWord.audioUrl || "",
    },
    tags: Array.isArray(source.tags) ? source.tags : defaults.tags,
    sections: {
      ...defaults.sections,
      ...rawSections,
      meaning: {
        ...defaults.sections.meaning,
        ...rawMeaning,
        meta: {
          ...defaults.sections.meaning.meta,
          ...rawMeaningMeta,
        },
      },
      examples: {
        ...defaults.sections.examples,
        ...rawExamples,
        items: Array.isArray(rawExamples.items)
          ? rawExamples.items.map((item, index) => {
              const rawExplain = item?.explain || {};
              return {
                id: item?.id || `example-${index + 1}`,
                sentence: item?.sentence || "",
                // TODO(后端待实现): 接口正式支持前，audioUrl 可能缺失，前端统一兜底为空字符串。
                audioUrl: item?.audioUrl || "",
                explain: {
                  collapsedByDefault:
                    rawExplain.collapsedByDefault === undefined
                      ? true
                      : !!rawExplain.collapsedByDefault,
                  reading: rawExplain.reading || "",
                  romaji: rawExplain.romaji || "",
                  meaningZh: rawExplain.meaningZh || "",
                  wordGrammarBreakdown: Array.isArray(
                    rawExplain.wordGrammarBreakdown
                  )
                    ? rawExplain.wordGrammarBreakdown
                    : [],
                  fixedPattern: {
                    pattern: rawExplain.fixedPattern?.pattern || "",
                    meaningZh: rawExplain.fixedPattern?.meaningZh || "",
                  },
                },
              };
            })
          : [],
      },
      synonyms: {
        ...defaults.sections.synonyms,
        ...rawSynonyms,
        items: Array.isArray(rawSynonyms.items) ? rawSynonyms.items : [],
      },
      related: {
        ...defaults.sections.related,
        ...rawRelated,
        items: Array.isArray(rawRelated.items) ? rawRelated.items : [],
      },
    },
    actions: Array.isArray(source.actions) ? source.actions : defaults.actions,
    progress: {
      ...defaults.progress,
      ...rawProgress,
      done: rawProgress.done === undefined ? !!source.done : !!rawProgress.done,
      hard: !!rawProgress.hard,
      favorite: !!rawProgress.favorite,
      status: rawProgress.status || defaults.progress.status,
      reviewCount:
        typeof rawProgress.reviewCount === "number"
          ? rawProgress.reviewCount
          : defaults.progress.reviewCount,
      correctCount:
        typeof rawProgress.correctCount === "number"
          ? rawProgress.correctCount
          : defaults.progress.correctCount,
      wrongCount:
        typeof rawProgress.wrongCount === "number"
          ? rawProgress.wrongCount
          : defaults.progress.wrongCount,
      lastReviewedAt: rawProgress.lastReviewedAt || null,
    },
    done: rawProgress.done === undefined ? !!source.done : !!rawProgress.done,
  };
}
