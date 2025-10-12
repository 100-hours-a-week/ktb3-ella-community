package com.example.ktb3community.common.pagination;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PaginationRequest(
        @NotNull @Min(1)
        int page,
        @Min(1) @Max(20)
        Integer pageSize
) {
}
