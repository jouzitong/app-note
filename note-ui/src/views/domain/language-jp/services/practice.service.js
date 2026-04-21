import practiceApi from "@/api/practices";
import {
  mapPracticeItemDtoToVm,
  mapPracticeStartDtoToVm,
  mapPracticeSubmitDtoToVm,
} from "@/mappers/domain/language-jp/practice.mapper";

export async function startPracticeSession(payload = {}) {
  const dto = await practiceApi.createPracticeSession(payload);
  return mapPracticeStartDtoToVm(dto || {});
}

export async function fetchPracticeItem(sessionId, index, userId) {
  const dto = await practiceApi.getPracticeItem(sessionId, index, userId);
  return mapPracticeItemDtoToVm(dto || {});
}

export async function submitPractice(sessionId, payload = {}) {
  const dto = await practiceApi.submitPracticeAnswer(sessionId, payload);
  return mapPracticeSubmitDtoToVm(dto || {});
}
