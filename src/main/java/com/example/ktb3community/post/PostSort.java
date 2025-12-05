package com.example.ktb3community.post;

import com.example.ktb3community.post.domain.Post;
import lombok.Getter;

import java.util.Comparator;
import java.util.function.ToLongFunction;

@Getter
public enum PostSort {
    LATEST(Post::getId),
    VIEW(Post::getViewCount),
    LIKE(Post::getLikeCount),
    CMT(Post::getCommentCount);

    private final ToLongFunction<Post> keyExtractor;

    PostSort(ToLongFunction<Post> keyExtractor) {
        this.keyExtractor = keyExtractor;
    }

    public long extractKey(Post post) {
        return keyExtractor.applyAsLong(post);
    }

    // cursorValue를 실제로 사용하는 정렬인지 여부
    public boolean usesCursorValue() {
        return this != LATEST;
    }

    // 인메모리 정렬용 공통 comparator
    public Comparator<Post> descendingComparator() {
        return Comparator
                .comparingLong(this::extractKey)
                .reversed()
                .thenComparing(Post::getId, Comparator.reverseOrder());
    }
}
