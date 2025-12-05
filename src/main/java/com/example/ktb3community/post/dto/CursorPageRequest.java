package com.example.ktb3community.post.dto;

import com.example.ktb3community.post.PostSort;

public record CursorPageRequest(
        Long cursorId,
        Long cursorValue,
        int limit,
        PostSort sort
) {}

