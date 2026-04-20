import { confirmWordCardDone, getWordCardPage } from "@/api/wordCards";
import {
  mapWordCardDtoToVm,
  mapWordCardPageDtoToVm,
} from "@/mappers/domain/language-jp/word-card.mapper";

export async function fetchWordCardPage({
  noteId,
  page = 1,
  size = 10,
  userId,
} = {}) {
  const pageDto = await getWordCardPage({
    noteId,
    page,
    size,
    userId,
  });
  return mapWordCardPageDtoToVm(pageDto || {});
}

export async function confirmWordCardDoneById(
  cardId,
  userId,
  fallbackWordCard
) {
  const responseDto = await confirmWordCardDone(cardId, userId);
  return mapWordCardDtoToVm(responseDto || fallbackWordCard || {});
}
