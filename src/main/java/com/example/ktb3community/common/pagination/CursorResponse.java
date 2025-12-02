package com.example.ktb3community.common.pagination;

import java.util.List;

public record CursorResponse<T>(
        List<T> content,
        Long nextCursorId,
        boolean hasNext
) {
}
