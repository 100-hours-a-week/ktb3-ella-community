package com.example.ktb3community.post;

import lombok.Getter;
import org.springframework.data.domain.Sort;

@Getter
public enum PostSort {
    NEW(Sort.Direction.DESC, "createdAt"),
    VIEW(Sort.Direction.DESC, "viewCount"),
    LIKE(Sort.Direction.DESC, "likeCount"),
    CMT(Sort.Direction.DESC, "commentCount");

    private final Sort sort;
    private final String property;

    PostSort(Sort.Direction direction, String property) {
        this.property = property;
        // 동일 정렬 값인 경우 id 오름차순으로 안정 정렬
        this.sort = Sort.by(new Sort.Order(direction, property))
                .and(Sort.by(Sort.Direction.DESC, "id"));
    }

    public Sort sort() {
        return sort;
    }
}
