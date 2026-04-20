export function mapPracticeSessionDtoToVm(source = {}) {
  if (!source || typeof source !== "object") {
    return null;
  }
  return source;
}

export function mapPracticeQuestionDtoToVm(source = {}) {
  if (!source || typeof source !== "object") {
    return null;
  }
  return source;
}

export function mapPracticeStartDtoToVm(source = {}) {
  return {
    session: mapPracticeSessionDtoToVm(source?.session),
    question: mapPracticeQuestionDtoToVm(source?.question),
  };
}

export function mapPracticeItemDtoToVm(source = {}) {
  return {
    session: mapPracticeSessionDtoToVm(source?.session),
    question: mapPracticeQuestionDtoToVm(source?.question),
  };
}

export function mapPracticeSubmitDtoToVm(source = {}) {
  return {
    result: source?.result || "",
    session: mapPracticeSessionDtoToVm(source?.session),
    nextQuestion: mapPracticeQuestionDtoToVm(source?.nextQuestion),
    correctAnswer: source?.correctAnswer || null,
    analysis: source?.analysis || null,
  };
}
