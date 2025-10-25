package com.example.ktb3community.common.pagination;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "페이지네이션 정보")
public record PageResponse<T>(
        @ArraySchema(schema = @Schema(description = "콘텐츠 목록"))
        List<T> content,
        @Schema(description = "현재 페이지 번호 (1부터 시작)", example = "1")
        int page,
        @Schema(description = "페이지 크기", example = "10")
        int pageSize,
        @Schema(description = "전체 페이지 수", example = "5")
        long totalPages
) {
}
