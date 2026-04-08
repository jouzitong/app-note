const TOKEN_KEY = "note_auth_token";
const USER_KEY = "note_auth_user";

function safeStorage() {
  if (typeof window === "undefined") {
    return null;
  }
  try {
    return window.localStorage;
  } catch (error) {
    return null;
  }
}

export function getAuthToken() {
  const storage = safeStorage();
  if (!storage) {
    return "";
  }
  return storage.getItem(TOKEN_KEY) || "";
}

export function hasAuthToken() {
  return Boolean(getAuthToken());
}

export function saveAuthToken(token) {
  const storage = safeStorage();
  if (!storage) {
    return;
  }
  if (!token) {
    storage.removeItem(TOKEN_KEY);
    return;
  }
  storage.setItem(TOKEN_KEY, token);
}

export function getAuthUser() {
  const storage = safeStorage();
  if (!storage) {
    return null;
  }
  const raw = storage.getItem(USER_KEY);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw);
  } catch (error) {
    return null;
  }
}

export function saveAuthUser(user) {
  const storage = safeStorage();
  if (!storage) {
    return;
  }
  if (!user) {
    storage.removeItem(USER_KEY);
    return;
  }
  storage.setItem(USER_KEY, JSON.stringify(user));
}

export function clearAuth() {
  const storage = safeStorage();
  if (!storage) {
    return;
  }
  storage.removeItem(TOKEN_KEY);
  storage.removeItem(USER_KEY);
}
