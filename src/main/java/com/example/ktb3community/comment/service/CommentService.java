package com.example.ktb3community.comment.service;

import com.example.ktb3community.comment.domain.Comment;
import com.example.ktb3community.comment.dto.CommentResponse;
import com.example.ktb3community.comment.dto.CreateCommentRequest;
import com.example.ktb3community.comment.exception.CommentNotFound;
import com.example.ktb3community.comment.repository.InMemoryCommentRepository;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.common.pagination.PageResponse;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.dto.Author;
import com.example.ktb3community.post.exception.PostNotFoundException;
import com.example.ktb3community.post.repository.InMemoryPostRepository;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.exception.UserNotFoundException;
import com.example.ktb3community.user.repository.InMemoryUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@AllArgsConstructor
public class CommentService {
    private final InMemoryUserRepository inMemoryUserRepository;
    private final InMemoryCommentRepository inMemoryCommentRepository;
    private final InMemoryPostRepository inMemoryPostRepository;

    private final int PAGE_SIZE = 10;

    public CommentResponse createComment(Long postId, Long userId, CreateCommentRequest createCommentRequest) {
        User user = inMemoryUserRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        if(!inMemoryPostRepository.existsById(postId)) {
            throw new PostNotFoundException();
        }
        Comment saved = inMemoryCommentRepository.save(Comment.createNew(postId, userId,
                createCommentRequest.content(), Instant.now()));
        Author author = new Author(user.getNickname(), user.getProfileImageUrl());
        return new CommentResponse(saved.getId(), saved.getContent(), author, saved.getCreatedAt());
    }

    public PageResponse<CommentResponse> getCommentList(long postId, int page){
        if(!inMemoryPostRepository.existsById(postId)) {
            throw new PostNotFoundException();
        }
        List<Comment> comments = inMemoryCommentRepository.findByPostId(postId).stream()
                .toList();

        int from = Math.max(0, (page - 1) * PAGE_SIZE);
        int to   = Math.min(comments.size(), from + PAGE_SIZE);
        List<Comment> slice = (from >= comments.size()) ? List.of() : comments.subList(from, to);
        long total = comments.size();
        long totalPages = (total + PAGE_SIZE - 1L) / PAGE_SIZE;
        List<CommentResponse> content = slice.stream().map(c -> {
            User u = inMemoryUserRepository.findById(c.getUserId())
                    .orElseThrow(UserNotFoundException::new);
            Author author = new Author(u.getNickname(), u.getProfileImageUrl());
            return new CommentResponse(
                    c.getId(),
                    c.getContent(),
                    author,
                    c.getCreatedAt()
            );
            }).toList();
        return new PageResponse<>(content, page, to, totalPages);
    }

    public CommentResponse updateComment(Long commentId, Long userId, CreateCommentRequest createCommentRequest) {
        Comment comment =  inMemoryCommentRepository.findById(commentId)
                .orElseThrow(CommentNotFound::new);
        User user = inMemoryUserRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        Post post = inMemoryPostRepository.findById(comment.getPostId())
                .orElseThrow(PostNotFoundException::new);
        if(!comment.getUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN);
        }
        comment.updateContent(createCommentRequest.content(), Instant.now());
        post.increaseComment();
        Author author = new Author(user.getNickname(), user.getProfileImageUrl());
        return new CommentResponse(comment.getId(), comment.getContent(), author, comment.getCreatedAt());
    }

    public void deleteComment(Long commentId, Long userId) {
        Comment comment =  inMemoryCommentRepository.findById(commentId)
                .orElseThrow(CommentNotFound::new);
        User user = inMemoryUserRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        Post post = inMemoryPostRepository.findById(comment.getPostId())
                .orElseThrow(PostNotFoundException::new);
        post.decreaseComment();
        if(!comment.getUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN);
        }
        comment.delete(Instant.now());
    }
}
