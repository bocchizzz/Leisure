package com.bangboo.common.security;

import java.util.Optional;

public final class CurrentUserContext {
    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    private CurrentUserContext() {
    }

    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    public static Optional<CurrentUser> get() {
        return Optional.ofNullable(HOLDER.get());
    }

    public static CurrentUser required() {
        return get().orElseThrow(() -> new IllegalStateException("Current user is not available"));
    }

    public static void clear() {
        HOLDER.remove();
    }
}
