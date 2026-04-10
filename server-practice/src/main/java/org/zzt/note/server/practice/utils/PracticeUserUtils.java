package org.zzt.note.server.practice.utils;

/**
 * 做题模块用户工具
 *
 * @author zhouzhitong
 * @since 2026/4/10
 */
public final class PracticeUserUtils {

    private static final long DEFAULT_USER_ID = 1L;

    private PracticeUserUtils() {
    }

    public static Long resolveUserId(Long userId) {
        if (userId == null || userId <= 0) {
            return DEFAULT_USER_ID;
        }
        return userId;
    }
}

