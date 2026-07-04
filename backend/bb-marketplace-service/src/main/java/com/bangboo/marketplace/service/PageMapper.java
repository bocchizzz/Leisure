package com.bangboo.marketplace.service;

import com.bangboo.common.response.PageResult;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Spring Data Page 到统一 PageResult 的转换工具。 */
public final class PageMapper {
    private PageMapper() {
    }

    public static <E, T> PageResult<T> toPageResult(Page<E> page, Function<E, T> mapper) {
        List<T> content = page.getContent().stream().map(mapper).toList();
        return new PageResult<>(
                content,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.isFirst(),
                page.isLast()
        );
    }

    public static <T> PageResult<T> of(List<T> content, int page, int size, long totalElements, int totalPages, boolean first, boolean last) {
        return new PageResult<>(content, totalElements, totalPages, page, size, first, last);
    }
}
