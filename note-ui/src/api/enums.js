import { requestJson } from "@/utils/http";

const API_BASE_URL = process.env.VUE_APP_API_BASE_URL || "/api";
const normalizedBase = API_BASE_URL.replace(/\/+$/, "");
const BASE_PATH = normalizedBase.endsWith("/api")
  ? `${normalizedBase}/common/v1/system/enums`
  : `${normalizedBase}/common/v1/system/enums`;

function unwrap(data) {
  if (data == null) {
    return data;
  }
  if (Object.prototype.hasOwnProperty.call(data, "data")) {
    return data.data;
  }
  if (Object.prototype.hasOwnProperty.call(data, "result")) {
    return data.result;
  }
  return data;
}

export async function fetchGlobalEnums() {
  const response = await requestJson(BASE_PATH);
  const body = unwrap(response);
  if (body && typeof body === "object") {
    return body;
  }
  return {};
}
