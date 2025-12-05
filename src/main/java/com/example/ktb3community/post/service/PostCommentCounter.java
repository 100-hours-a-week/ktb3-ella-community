package com.example.ktb3community.post.service;

import com.example.ktb3community.post.domain.Post;

public interface PostCommentCounter {
    void increaseCommentCount(Post post);
    void decreaseCommentCount(Post post);
}
