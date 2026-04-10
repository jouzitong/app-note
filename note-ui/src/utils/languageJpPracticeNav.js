const LAST_PRACTICE_NODE_ID_KEY = "language_jp_last_practice_node_id";

function getStorage() {
  if (typeof window === "undefined") {
    return null;
  }
  try {
    return window.localStorage;
  } catch (error) {
    return null;
  }
}

function normalizeNodeId(nodeId) {
  const value = Number(nodeId);
  if (!Number.isInteger(value) || value <= 0) {
    return null;
  }
  return value;
}

export function saveLastLanguageJpPracticeNodeId(nodeId) {
  const normalized = normalizeNodeId(nodeId);
  if (normalized === null) {
    return;
  }
  const storage = getStorage();
  if (!storage) {
    return;
  }
  storage.setItem(LAST_PRACTICE_NODE_ID_KEY, String(normalized));
}

export function getLastLanguageJpPracticeNodeId(defaultNodeId) {
  const storage = getStorage();
  if (!storage) {
    return defaultNodeId;
  }
  const raw = storage.getItem(LAST_PRACTICE_NODE_ID_KEY);
  const normalized = normalizeNodeId(raw);
  return normalized === null ? defaultNodeId : normalized;
}
