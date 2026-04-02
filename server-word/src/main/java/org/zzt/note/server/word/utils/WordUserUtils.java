package org.zzt.note.server.word.utils;

/**
 * 单词模块用户工具
 *
 * @author zhouzhitong
 * @since 2026/4/2
 */
public final class WordUserUtils {

    private static final long DEFAULT_USER_ID = 1L;

    private WordUserUtils() {
    }

    /**
     * 解析用户ID（后续可替换为登录态解析）
     *
     * @param userId 外部传入用户ID
     * @return 可用用户ID
     */
    public static Long resolveUserId(Long userId) {
        if (userId == null || userId <= 0) {
            return DEFAULT_USER_ID;
        }
        return userId;
    }
}
