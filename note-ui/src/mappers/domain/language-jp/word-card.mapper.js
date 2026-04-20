import {
  createDefaultWordCard,
  createMockWordCard,
  normalizeWordCard,
} from "@/model/word/wordCard";

export function createDefaultWordCardVm() {
  return createDefaultWordCard();
}

export function createMockWordCardVm() {
  return createMockWordCard();
}

export function mapWordCardDtoToVm(source = {}) {
  return normalizeWordCard(source);
}

export function mapWordCardPageDtoToVm(pageResult = {}) {
  const records = Array.isArray(pageResult?.records) ? pageResult.records : [];
  return {
    records: records.map((item) => mapWordCardDtoToVm(item)),
    pageInfo: pageResult?.pageInfo || null,
    raw: pageResult?.raw || null,
  };
}
