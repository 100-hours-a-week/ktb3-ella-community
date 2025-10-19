package com.example.ktb3community.post.service;

public interface PostCommentCounter {
    void increaseCommentCount(Long postId);
    void decreaseCommentCount(Long postId);
}
