package com.example.ktb3community.comment.service;

import com.example.ktb3community.comment.domain.Comment;
import com.example.ktb3community.comment.dto.CommentResponse;
import com.example.ktb3community.comment.dto.CreateCommentRequest;
import com.example.ktb3community.comment.mapper.CommentMapper;
import com.example.ktb3community.comment.repository.CommentRepository;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.common.pagination.PageResponse;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.repository.PostRepository;
import com.example.ktb3community.post.service.PostCommentCounter;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.exception.UserNotFoundException;
import com.example.ktb3community.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CommentService {
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final PostCommentCounter postCommentCounter;
    private final CommentMapper commentMapper;

    private static final int PAGE_SIZE = 10;

    @Transactional
    public CommentResponse createComment(Long postId, Long userId, CreateCommentRequest createCommentRequest) {
        User user = userRepository.findByIdOrThrow(userId);
        Post post = postRepository.findByIdOrThrow(postId);
        Comment saved = commentRepository.save(Comment.createNew(post, user,
                createCommentRequest.content(), Instant.now()));
        postCommentCounter.increaseCommentCount(postId);
        return commentMapper.toCommentResponse(saved, user);
    }

    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> getCommentList(long postId, int page){
        Post post = postRepository.findByIdOrThrow(postId);
        List<Comment> comments = commentRepository.findByPost(post).stream()
                .toList();

        int from = Math.max(0, (page - 1) * PAGE_SIZE);
        List<Comment> slice = (from >= comments.size()) ? List.of() : comments.subList(from, Math.min(comments.size(), from + PAGE_SIZE));
        long total = comments.size();
        long totalPages = (total + PAGE_SIZE - 1L) / PAGE_SIZE;

        Set<Long> authorIds = slice.stream()
                .map(Comment::getUserId)
                .collect(Collectors.toSet());

        Map<Long, User> authorMap = userRepository.findAllByIdIn(authorIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        List<CommentResponse> content = slice.stream().map(c -> {
            User user = authorMap.get(c.getUserId());
            if(user == null){
                throw new UserNotFoundException();
            }
            return commentMapper.toCommentResponse(c, user);
            }).toList();
        return new PageResponse<>(content, page, PAGE_SIZE, totalPages);
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, Long userId, CreateCommentRequest createCommentRequest) {
        Comment comment =  commentRepository.findByIdOrThrow(commentId);
        User user = userRepository.findByIdOrThrow(userId);
        if(!comment.getUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN);
        }
        comment.updateContent(createCommentRequest.content(), Instant.now());
        commentRepository.save(comment);
        return commentMapper.toCommentResponse(comment, user);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment =  commentRepository.findByIdOrThrow(commentId);
        User user = userRepository.findByIdOrThrow(userId);
        Post post = postRepository.findByIdOrThrow(comment.getPostId());
        if(!comment.getUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN);
        }
        comment.delete(Instant.now());
        postCommentCounter.decreaseCommentCount(post.getId());
    }
}
