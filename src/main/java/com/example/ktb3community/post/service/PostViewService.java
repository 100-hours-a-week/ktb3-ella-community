package com.example.ktb3community.post.service;

import com.example.ktb3community.comment.dto.CommentResponse;
import com.example.ktb3community.comment.service.CommentService;
import com.example.ktb3community.common.pagination.CursorResponse;
import com.example.ktb3community.common.pagination.PageResponse;
import com.example.ktb3community.post.PostSort;
import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.dto.Author;
import com.example.ktb3community.post.dto.PostDetailResponse;
import com.example.ktb3community.post.dto.PostListResponse;
import com.example.ktb3community.post.repository.PostLikeRepository;
import com.example.ktb3community.post.repository.PostRepository;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

        Pageable pageable = PageRequest.of(0, size + 1);

        List<Post> posts = postRepository.findAllByCursor(cursorId, cursorValue, sort, pageable);

        boolean hasNext = false;
        if (posts.size() > size) {
            hasNext = true;
            posts.remove(size);
        }

        Long nextCursorId = null;
        Long nextCursorValue = null;

        if (!posts.isEmpty()) {
            Post lastPost = posts.getLast();
            nextCursorId = lastPost.getId();

            nextCursorValue = switch (sort) {
                case VIEW -> lastPost.getViewCount();
                case LIKE -> lastPost.getLikeCount();
                case CMT -> lastPost.getCommentCount();
                default -> null;
            };
        }

        List<PostListResponse> content = posts.stream().map(p -> {
            User user = p.getUser();
            Author author = new Author(user.getNickname(), user.getProfileImageUrl());
            return new PostListResponse(
                    p.getId(),
                    p.getTitle(),
                    author,
                    p.getLikeCount(),
                    p.getViewCount(),
                    p.getCommentCount(),
                    p.getCreatedAt()
            );
        }).toList();
        return new CursorResponse<>(content, nextCursorId, nextCursorValue, hasNext);
    }

    @Transactional
    public PostDetailResponse getPostDetail(long postId, long userId) {
        Post post = postRepository.findByIdOrThrow(postId);
        User authorUser = userRepository.findByIdOrThrow(post.getUserId());
        Author author = new Author(authorUser.getNickname(), authorUser.getProfileImageUrl());
        User viewer = userRepository.findByIdOrThrow(userId);
        PageResponse<CommentResponse> commentsPage =
                commentService.getCommentList(postId, COMMENT_PAGE);
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
