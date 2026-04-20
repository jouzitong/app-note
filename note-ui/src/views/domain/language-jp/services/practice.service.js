import {
  createPracticeSession,
  getPracticeItem,
  submitPracticeAnswer,
} from "@/api/practices";
import {
  mapPracticeItemDtoToVm,
  mapPracticeStartDtoToVm,
  mapPracticeSubmitDtoToVm,
} from "@/mappers/domain/language-jp/practice.mapper";

export async function startPracticeSession(payload = {}) {
  const dto = await createPracticeSession(payload);
  return mapPracticeStartDtoToVm(dto || {});
}

export async function fetchPracticeItem(sessionId, index, userId) {
  const dto = await getPracticeItem(sessionId, index, userId);
  return mapPracticeItemDtoToVm(dto || {});
}

export async function submitPractice(sessionId, payload = {}) {
  const dto = await submitPracticeAnswer(sessionId, payload);
  return mapPracticeSubmitDtoToVm(dto || {});
}
