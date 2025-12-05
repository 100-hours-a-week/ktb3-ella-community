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
import com.example.ktb3community.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

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
    public CommentResponse createComment(long postId, long userId, CreateCommentRequest createCommentRequest) {
        Post post = postRepository.findByIdOrThrow(postId);
        User user = userRepository.findByIdOrThrow(userId);
        Comment saved = commentRepository.save(Comment.createNew(post, user,
                createCommentRequest.content()));
        postCommentCounter.increaseCommentCount(post);
        return commentMapper.toCommentResponse(saved, user);
    }

    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> getCommentList(long postId, int page){
        Post post = postRepository.findByIdOrThrow(postId);
        int requestedPage = Math.max(page, 1);
        PageRequest pageRequest = PageRequest.of(requestedPage - 1, PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.DESC, "id")));

        Page<Comment> commentPage = commentRepository.findByPost(post, pageRequest);

        Page<CommentResponse> mapped = commentPage.map(c -> {
            User user = c.getUser();
            return commentMapper.toCommentResponse(c, user);
        });

        return new PageResponse<>(
                mapped.getContent(),
                mapped.getNumber() + 1,
                mapped.getSize(),
                mapped.getTotalPages()
        );
    }

    @Transactional
    public CommentResponse updateComment(long commentId, long userId, CreateCommentRequest createCommentRequest) {
        Comment comment =  commentRepository.findByIdOrThrow(commentId);
        if(!comment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN);
        }
        comment.updateContent(createCommentRequest.content());
        User user = comment.getUser();
        return commentMapper.toCommentResponse(comment, user);
    }

    @Transactional
    public void deleteComment(long commentId, long userId) {
        Comment comment =  commentRepository.findByIdOrThrow(commentId);
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN);
        }
        comment.delete(Instant.now());

        Post post = postRepository.findByIdOrThrow(comment.getPostId());
        postCommentCounter.decreaseCommentCount(post);
    }
}
