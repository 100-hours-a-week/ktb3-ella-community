package com.example.ktb3community.post.service;

import com.example.ktb3community.comment.dto.CommentResponse;
import com.example.ktb3community.comment.service.CommentService;
import com.example.ktb3community.common.pagination.PageResponse;
import com.example.ktb3community.post.PostSort;
import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.dto.Author;
import com.example.ktb3community.post.dto.PostDetailResponse;
import com.example.ktb3community.post.dto.PostListResponse;
import com.example.ktb3community.post.repository.InMemoryPostLikeRepository;
import com.example.ktb3community.post.repository.InMemoryPostRepository;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.exception.UserNotFoundException;
import com.example.ktb3community.user.repository.InMemoryUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PostViewService {
    private final InMemoryPostRepository inMemoryPostRepository;
    private final InMemoryUserRepository inMemoryUserRepository;
    private final InMemoryPostLikeRepository inMemoryPostLikeRepository;
    private final CommentService commentService;

    private static final int COMMENT_PAGE = 1;

    public PageResponse<PostListResponse> getPostList(int page, int pageSize, PostSort sort) {
        Comparator<Post> postComparator = switch (sort) {
            case VIEW -> Comparator.comparingLong(Post::getViewCount).reversed();
            case LIKE -> Comparator.comparingLong(Post::getLikeCount).reversed();
            case CMT  -> Comparator.comparingLong(Post::getCommentCount).reversed();
            case NEW  -> Comparator.comparing(Post::getCreatedAt).reversed();
        };
        List<Post> posts = inMemoryPostRepository.findAll().stream()
                .sorted(postComparator)
                .toList();

        int from = Math.max(0, (page - 1) * pageSize);
        int to   = Math.min(posts.size(), from + pageSize);
        List<Post> slice = (from >= posts.size()) ? List.of() : posts.subList(from, to);
        long total = posts.size();
        long totalPages = (total + pageSize - 1L) / pageSize;

        Set<Long> authorIds = slice.stream()
                .map(Post::getUserId)
                .collect(Collectors.toSet());

        Map<Long, User> authorMap = inMemoryUserRepository.findAllByIdIn(authorIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        List<PostListResponse> content = slice.stream().map(p -> {
            User user = authorMap.get(p.getUserId());
            if(user == null){
                throw new UserNotFoundException();
            }
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
        return new PageResponse<>(content, page, pageSize, totalPages);
    }

    public PostDetailResponse getPostDetail(long postId, long userId) {
        Post post = inMemoryPostRepository.findByIdOrThrow(postId);
        User user = inMemoryUserRepository.findByIdOrThrow(post.getUserId());
        Author author = new Author(user.getNickname(), user.getProfileImageUrl());
        inMemoryUserRepository.findByIdOrThrow(userId);
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
                inMemoryPostLikeRepository.exists(postId, userId),
                post.getCreatedAt(),
                commentsPage
        );
    }
}
