import appModule from "@/store/modules/app";

describe("store/modules/app", () => {
  test("enumByKey returns array or empty array", () => {
    const state = {
      globalEnums: {
        noteType: [{ code: 1, label: "MARKDOWN" }],
      },
    };
    const getter = appModule.getters.enumByKey(state);

    expect(getter("noteType")).toEqual([{ code: 1, label: "MARKDOWN" }]);
    expect(getter("missing")).toEqual([]);
    expect(getter("")).toEqual([]);
  });

  test("SET_GLOBAL_ENUMS stores object and normalizes invalid input", () => {
    const state = {
      globalEnums: {},
    };

    appModule.mutations.SET_GLOBAL_ENUMS(state, { a: 1 });
    expect(state.globalEnums).toEqual({ a: 1 });

    appModule.mutations.SET_GLOBAL_ENUMS(state, null);
    expect(state.globalEnums).toEqual({});
  });

  test("SHOW_TOAST and HIDE_TOAST update toast state", () => {
    const state = {
      toast: {
        visible: false,
        message: "",
        type: "error",
      },
    };

    appModule.mutations.SHOW_TOAST(state, { message: " hello ", type: "info" });
    expect(state.toast.visible).toBe(true);
    expect(state.toast.message).toBe("hello");
    expect(state.toast.type).toBe("info");

    appModule.mutations.HIDE_TOAST(state);
    expect(state.toast.visible).toBe(false);
  });
});
