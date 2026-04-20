import { resolveErrorMessageByCode } from "@/constants/error-map";
import { extractErrorCode, getErrorMessage } from "@/utils/error";

describe("utils/error", () => {
  test("resolveErrorMessageByCode returns mapped message", () => {
    expect(resolveErrorMessageByCode("UNAUTHORIZED")).toBe(
      "登录已过期，请重新登录"
    );
    expect(resolveErrorMessageByCode("UNKNOWN_CODE")).toBe("");
  });

  test("extractErrorCode reads from bodyJson and fallback statusText", () => {
    expect(extractErrorCode({ bodyJson: { code: "TOKEN_EXPIRED" } })).toBe(
      "TOKEN_EXPIRED"
    );
    expect(
      extractErrorCode({ bodyJson: { data: { errorCode: "FORBIDDEN" } } })
    ).toBe("FORBIDDEN");
    expect(extractErrorCode({ statusText: "NETWORK_ERROR" })).toBe(
      "NETWORK_ERROR"
    );
  });

  test("getErrorMessage prefers code mapping then body/raw message", () => {
    expect(
      getErrorMessage({
        bodyJson: { code: "INVALID_ARGUMENT", msg: "原始错误" },
      })
    ).toBe("请求参数有误，请检查后重试");

    expect(getErrorMessage({ bodyJson: { msg: "后端提示" } })).toBe("后端提示");
    expect(getErrorMessage({ bodyText: "文本错误" })).toBe("文本错误");
    expect(getErrorMessage({ message: "异常消息" })).toBe("异常消息");
  });
});
