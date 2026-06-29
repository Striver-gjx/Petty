package com.petty.common.security;

import lombok.Getter;

/**
 * 存储当前请求用户上下文（线程隔离）
 */
public class UserContext {

    private static final ThreadLocal<UserInfo> HOLDER = new ThreadLocal<>();

    public static void set(UserInfo user) {
        HOLDER.set(user);
    }

    public static UserInfo get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        UserInfo info = HOLDER.get();
        return info != null ? info.getUserId() : null;
    }

    public static String getRole() {
        UserInfo info = HOLDER.get();
        return info != null ? info.getRole() : null;
    }

    public static void clear() {
        HOLDER.remove();
    }

    @Getter
    public static class UserInfo {
        private final Long userId;
        private final String role;

        public UserInfo(Long userId, String role) {
            this.userId = userId;
            this.role = role;
        }
    }
}
