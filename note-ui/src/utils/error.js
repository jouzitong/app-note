import { resolveErrorMessageByCode } from "@/constants/error-map";

export function extractErrorCode(error) {
  const bodyJson = error?.bodyJson;
  if (bodyJson && typeof bodyJson === "object") {
    const directCode = bodyJson.code || bodyJson.errorCode;
    if (directCode !== undefined && directCode !== null && directCode !== "") {
      return String(directCode).trim();
    }
    const dataCode = bodyJson.data?.code || bodyJson.data?.errorCode;
    if (dataCode !== undefined && dataCode !== null && dataCode !== "") {
      return String(dataCode).trim();
    }
  }
  const statusText = error?.statusText;
  if (typeof statusText === "string" && statusText.trim()) {
    return statusText.trim();
  }
  return "";
}

export function getErrorMessage(error) {
  const mappedByCode = resolveErrorMessageByCode(extractErrorCode(error));
  if (mappedByCode) {
    return mappedByCode;
  }

  const bodyJson = error?.bodyJson;
  if (bodyJson && typeof bodyJson === "object") {
    const topMessage = bodyJson.msg || bodyJson.message;
    if (typeof topMessage === "string" && topMessage.trim()) {
      return topMessage.trim();
    }
    const dataMessage = bodyJson.data?.msg || bodyJson.data?.message;
    if (typeof dataMessage === "string" && dataMessage.trim()) {
      return dataMessage.trim();
    }
  }

  const bodyText = error?.bodyText;
  if (typeof bodyText === "string" && bodyText.trim()) {
    try {
      const parsed = JSON.parse(bodyText);
      const parsedMessage = parsed?.msg || parsed?.message;
      if (typeof parsedMessage === "string" && parsedMessage.trim()) {
        return parsedMessage.trim();
      }
    } catch (_) {
      // ignore parse error
    }
    return bodyText.trim();
  }

  const message = error?.message;
  if (typeof message === "string" && message.trim()) {
    return message.trim();
  }
  return "";
}
