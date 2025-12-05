package com.example.ktb3community.post.service;

import com.example.ktb3community.comment.dto.CommentResponse;
import com.example.ktb3community.comment.service.CommentService;
import com.example.ktb3community.common.pagination.CursorResponse;
import com.example.ktb3community.common.pagination.PageResponse;
import com.example.ktb3community.post.PostSort;
import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.dto.Author;
import com.example.ktb3community.post.dto.CursorPageRequest;
import com.example.ktb3community.post.dto.PostDetailResponse;
import com.example.ktb3community.post.dto.PostListResponse;
import com.example.ktb3community.post.repository.PostLikeRepository;
import com.example.ktb3community.post.repository.PostRepository;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class PostViewService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentService commentService;

    private static final int COMMENT_PAGE = 1;

    public CursorResponse<PostListResponse> getPostList(Long cursorId, Long cursorValue, int size, PostSort sort) {

        int limit = size + 1;

        CursorPageRequest request = new CursorPageRequest(
                cursorId,
                cursorValue,
                limit,
                sort
        );

        List<Post> posts = postRepository.findAllByCursor(request);

        boolean hasNext = posts.size() > size;
        if (hasNext) {
            posts = posts.subList(0, size);
        }

        Long nextCursorId = null;
        Long nextCursorValue = null;

        if (!posts.isEmpty()) {
            Post lastPost = posts.getLast();
            nextCursorId = lastPost.getId();
            nextCursorValue = sort.usesCursorValue()
                    ? sort.extractKey(lastPost)
                    : null;
        }

        List<PostListResponse> content = posts.stream().map(post -> {
            User user = post.getUser();
            Author author = new Author(user.getNickname(), user.getProfileImageUrl());
            return new PostListResponse(
                    post.getId(),
                    post.getTitle(),
                    author,
                    post.getLikeCount(),
                    post.getViewCount(),
                    post.getCommentCount(),
                    post.getCreatedAt()
            );
        }).toList();
        return new CursorResponse<>(content, nextCursorId, nextCursorValue, hasNext);
    }

    @Transactional
    public PostDetailResponse getPostDetail(long postId, long userId) {
        Post post = postRepository.findByIdOrThrow(postId);
        User user = post.getUser();
        Author author = new Author(user.getNickname(), user.getProfileImageUrl());
        PageResponse<CommentResponse> commentsPage =
                commentService.getCommentList(postId, COMMENT_PAGE);
        User viewer = userRepository.findByIdOrThrow(userId);
        post.increaseViewCount();
        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getPostImageUrl(),
                author,
                post.getLikeCount(),
                post.getViewCount(),
                post.getCommentCount(),
                postLikeRepository.exists(post, viewer),
                post.getCreatedAt(),
                commentsPage
        );
    }
}
