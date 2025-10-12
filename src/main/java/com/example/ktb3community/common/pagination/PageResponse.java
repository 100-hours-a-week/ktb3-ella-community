package com.example.ktb3community.common.pagination;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int pageSize,
        long totalPages
) {
}
