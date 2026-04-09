const LAST_NOTE_ID_KEY = "language_jp_last_note_id";
const DEFAULT_NOTE_ID = 1;

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

function normalizeNoteId(noteId) {
  const value = Number(noteId);
  if (!Number.isInteger(value) || value <= 0) {
    return null;
  }
  return value;
}

export function saveLastLanguageJpNoteId(noteId) {
  const normalized = normalizeNoteId(noteId);
  if (normalized === null) {
    return;
  }
  const storage = getStorage();
  if (!storage) {
    return;
  }
  storage.setItem(LAST_NOTE_ID_KEY, String(normalized));
}

export function getLastLanguageJpNoteId() {
  const storage = getStorage();
  if (!storage) {
    return DEFAULT_NOTE_ID;
  }
  const raw = storage.getItem(LAST_NOTE_ID_KEY);
  const normalized = normalizeNoteId(raw);
  return normalized === null ? DEFAULT_NOTE_ID : normalized;
}

export function buildLanguageJpNotePath(noteId) {
  const normalized = normalizeNoteId(noteId) || getLastLanguageJpNoteId();
  return `/language-jp/note/${normalized}`;
}
