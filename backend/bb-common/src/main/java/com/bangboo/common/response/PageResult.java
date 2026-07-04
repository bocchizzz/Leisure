package com.bangboo.common.response;

import java.util.List;

public record PageResult<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int number,
        int size,
        boolean first,
        boolean last
) {
    public static <T> PageResult<T> empty(int page, int size) {
        return new PageResult<>(List.of(), 0, 0, page, size, true, true);
    }
}
