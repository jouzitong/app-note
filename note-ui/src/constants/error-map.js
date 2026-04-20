export const ERROR_MESSAGE_MAP = Object.freeze({
  UNAUTHORIZED: "登录已过期，请重新登录",
  FORBIDDEN: "当前账号无权限执行该操作",
  NOT_FOUND: "请求资源不存在",
  RATE_LIMITED: "请求过于频繁，请稍后重试",
  INTERNAL_ERROR: "系统繁忙，请稍后重试",
  NETWORK_ERROR: "网络异常，请检查网络后重试",
  TOKEN_EXPIRED: "登录状态已失效，请重新登录",
  INVALID_ARGUMENT: "请求参数有误，请检查后重试",
});

export function resolveErrorMessageByCode(code) {
  if (code === null || code === undefined || code === "") {
    return "";
  }
  const normalized = String(code).trim();
  if (!normalized) {
    return "";
  }
  return ERROR_MESSAGE_MAP[normalized] || "";
}
