import { fetchLanguageJpOverview } from "@/views/domain/language-jp/services/language-jp.service";

describe("language-jp.service", () => {
  test("fetchLanguageJpOverview returns default shape", async () => {
    const result = await fetchLanguageJpOverview();
    expect(Array.isArray(result.items)).toBe(true);
  });
});
