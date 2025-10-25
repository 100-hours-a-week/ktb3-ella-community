package com.example.ktb3community.post;

import com.example.ktb3community.post.domain.Post;

import java.util.Comparator;

public enum PostSort {
    NEW(Comparator.comparing(Post::getCreatedAt).reversed()),
    VIEW(Comparator.comparing(Post::getViewCount).reversed()),
    LIKE(Comparator.comparing(Post::getLikeCount).reversed()),
    CMT(Comparator.comparing(Post::getCommentCount).reversed());

    private final Comparator<Post> comparator;

    PostSort(Comparator<Post> comparator) {
        // 동률일 때 id로 추가 정렬
        this.comparator = comparator.thenComparing(Post::getId);
    }
    public Comparator<Post> comparator() {
        return comparator;
    }
}
