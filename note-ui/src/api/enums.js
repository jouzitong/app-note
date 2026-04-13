import { requestJson, unwrapResponse } from "@/utils/http";

const BASE_PATH = "/common/v1/system/enums";

export async function fetchGlobalEnums() {
  const response = await requestJson(BASE_PATH);
  const body = unwrapResponse(response);
  if (body && typeof body === "object") {
    return body;
  }
  return {};
}
