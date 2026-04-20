import store from "@/store";

export const DEFAULT_TOAST_DURATION_MS = 750;

function normalizeToastPayload(payload = {}) {
  const message = (payload?.message || "").trim();
  if (!message) {
    return null;
  }
  const type = payload?.type || "info";
  const durationMs =
    typeof payload?.durationMs === "number"
      ? payload.durationMs
      : DEFAULT_TOAST_DURATION_MS;
  return {
    message,
    type,
    durationMs,
  };
}

export function showToast(payload = {}) {
  const normalized = normalizeToastPayload(payload);
  if (!normalized) {
    return Promise.resolve();
  }
  return store.dispatch("app/showToast", normalized);
}

export function showSuccessToast(
  message,
  durationMs = DEFAULT_TOAST_DURATION_MS
) {
  return showToast({ message, type: "success", durationMs });
}

export function showInfoToast(message, durationMs = DEFAULT_TOAST_DURATION_MS) {
  return showToast({ message, type: "info", durationMs });
}

export function showErrorToast(
  message,
  durationMs = DEFAULT_TOAST_DURATION_MS
) {
  return showToast({ message, type: "error", durationMs });
}
