export const DEFAULT_TAG_CLASS_NAME = "app-tag--secondary";

export const TAG_STYLE_PRESETS = Object.freeze([
  {
    className: "app-tag--primary",
    label: "Primary",
    textColor: "#084298",
    backgroundColor: "#cfe2ff",
    borderColor: "#9ec5fe",
  },
  {
    className: "app-tag--secondary",
    label: "Secondary",
    textColor: "#41464b",
    backgroundColor: "#e2e3e5",
    borderColor: "#c4c8cb",
  },
  {
    className: "app-tag--success",
    label: "Success",
    textColor: "#0f5132",
    backgroundColor: "#d1e7dd",
    borderColor: "#a3cfbb",
  },
  {
    className: "app-tag--danger",
    label: "Danger",
    textColor: "#842029",
    backgroundColor: "#f8d7da",
    borderColor: "#f1aeb5",
  },
  {
    className: "app-tag--warning",
    label: "Warning",
    textColor: "#664d03",
    backgroundColor: "#fff3cd",
    borderColor: "#ffe69c",
  },
  {
    className: "app-tag--info",
    label: "Info",
    textColor: "#055160",
    backgroundColor: "#cff4fc",
    borderColor: "#9eeaf9",
  },
  {
    className: "app-tag--light",
    label: "Light",
    textColor: "#495057",
    backgroundColor: "#fcfcfd",
    borderColor: "#f1f3f5",
  },
  {
    className: "app-tag--dark",
    label: "Dark",
    textColor: "#e9ecef",
    backgroundColor: "#343a40",
    borderColor: "#1f2327",
  },
]);

export const TAG_STYLE_PRESET_MAP = Object.freeze(
  TAG_STYLE_PRESETS.reduce((acc, item) => {
    acc[item.className] = item;
    return acc;
  }, {})
);

export function resolveTagStylePreset(className) {
  if (!className) {
    return TAG_STYLE_PRESET_MAP[DEFAULT_TAG_CLASS_NAME];
  }
  return (
    TAG_STYLE_PRESET_MAP[className] ||
    TAG_STYLE_PRESET_MAP[DEFAULT_TAG_CLASS_NAME]
  );
}
