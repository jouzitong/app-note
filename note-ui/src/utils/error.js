export function getErrorMessage(error) {
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
