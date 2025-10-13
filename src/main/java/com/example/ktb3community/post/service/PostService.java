package com.example.ktb3community.post.service;

import com.example.ktb3community.comment.dto.CommentResponse;
import com.example.ktb3community.comment.service.CommentService;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.common.pagination.PageResponse;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.post.PostSort;
import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.dto.*;
import com.example.ktb3community.post.exception.PostNotFoundException;
import com.example.ktb3community.post.repository.InMemoryPostLikeRepository;
import com.example.ktb3community.post.repository.InMemoryPostRepository;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.exception.UserNotFoundException;
import com.example.ktb3community.user.repository.InMemoryUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
public class PostService {
    private final InMemoryPostRepository inMemoryPostRepository;
    private final InMemoryUserRepository inMemoryUserRepository;
    private final InMemoryPostLikeRepository inMemoryPostLikeRepository;
    private final CommentService commentService;

    public CreatePostResponse createPost(Long userId, CreatePostRequest createPostRequest) {
        if(!inMemoryUserRepository.existsById(userId)){
            throw new UserNotFoundException();
        }
        Post saved = inMemoryPostRepository.save(Post.createNew(userId, createPostRequest.title(),
                createPostRequest.content(), createPostRequest.postImageUrl(), Instant.now()));
        return new CreatePostResponse(saved.getId());
    }

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
        List<PostListResponse> content = slice.stream().map(p -> {
            User u = inMemoryUserRepository.findById(p.getUserId())
                    .orElseThrow(UserNotFoundException::new);
            Author author = new Author(u.getNickname(), u.getProfileImageUrl());
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
        Post post = inMemoryPostRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);
        User user = inMemoryUserRepository.findById(post.getUserId())
                .orElseThrow(UserNotFoundException::new);
        Author author = new Author(user.getNickname(), user.getProfileImageUrl());
        PageResponse<CommentResponse> commentsPage =
                commentService.getCommentList(postId, 1);
        post.increaseView();
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

    public CreatePostResponse updatePost(Long postId, Long userId, CreatePostRequest createPostRequest) {
        if(!inMemoryUserRepository.existsById(userId)){
            throw new UserNotFoundException();
        }
        Post post = inMemoryPostRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);
        if(!post.getUserId().equals(userId)){
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN);
        }
        post.updatePost(createPostRequest.title(), createPostRequest.content(), createPostRequest.postImageUrl(), Instant.now());
        return new CreatePostResponse(post.getId());
    }

    public void deletePost(Long postId, Long userId) {
        if(!inMemoryUserRepository.existsById(userId)){
            throw new UserNotFoundException();
        }
        Post post = inMemoryPostRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);
        if(!post.getUserId().equals(userId)){
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN);
        }
        post.delete(Instant.now());
    }
}
