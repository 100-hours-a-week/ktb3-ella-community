package com.example.ktb3community.comment;

import com.example.ktb3community.comment.domain.Comment;
import com.example.ktb3community.comment.dto.CommentResponse;
import com.example.ktb3community.comment.dto.CreateCommentRequest;
import com.example.ktb3community.comment.mapper.CommentMapper;
import com.example.ktb3community.comment.repository.CommentRepository;
import com.example.ktb3community.comment.service.CommentService;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.common.pagination.PageResponse;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.repository.PostRepository;
import com.example.ktb3community.post.service.PostCommentCounter;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.exception.UserNotFoundException;
import com.example.ktb3community.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock UserRepository userRepository;
    @Mock CommentRepository commentRepository;
    @Mock PostRepository postRepository;
    @Mock PostCommentCounter postCommentCounter;
    @Mock CommentMapper commentMapper;

    @InjectMocks
    CommentService commentService;

    private User newUser(Long id) {
        User user = User.builder().nickname("nick").build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Post newPost(Long id) {
        Post post = Post.builder().build();
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }

    private Comment newComment(Long id, Post post, User user, String content) {
        Comment comment = Comment.createNew(post, user, content);
        ReflectionTestUtils.setField(comment, "id", id);
        return comment;
    }

    @Test
    @DisplayName("createComment: 댓글 저장 후 카운트를 증가시키고 응답을 반환한다")
    void createComment_success() {
        Long postId = 10L;
        Long userId = 1L;
        CreateCommentRequest request = new CreateCommentRequest("New Comment");

        User user = newUser(userId);
        Post post = newPost(postId);
        Comment savedComment = newComment(100L, post, user, "New Comment");

        given(userRepository.findByIdOrThrow(userId)).willReturn(user);
        given(postRepository.findByIdOrThrow(postId)).willReturn(post);

        given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

        CommentResponse expectedResponse = new CommentResponse(100L, "New Comment", null, null);
        given(commentMapper.toCommentResponse(savedComment, user)).willReturn(expectedResponse);

        CommentResponse response = commentService.createComment(postId, userId, request);

        assertThat(response).isEqualTo(expectedResponse);

        verify(postCommentCounter).increaseCommentCount(postId);

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        assertThat(captor.getValue().getContent()).isEqualTo("New Comment");
    }

    @Test
    @DisplayName("getCommentList: 댓글 목록과 작성자 정보를 매핑하여 반환한다")
    void getCommentList_success() {
        Long postId = 10L;
        Post post = newPost(postId);
        User user1 = newUser(1L);
        User user2 = newUser(2L);

        Comment c1 = newComment(100L, post, user1, "Content1");
        Comment c2 = newComment(101L, post, user2, "Content2");

        Page<Comment> commentPage = new PageImpl<>(List.of(c1, c2));

        given(postRepository.findByIdOrThrow(postId)).willReturn(post);
        given(commentRepository.findByPost(any(Post.class), any(PageRequest.class))).willReturn(commentPage);

        given(userRepository.findAllByIdIn(Set.of(1L, 2L))).willReturn(List.of(user1, user2));

        given(commentMapper.toCommentResponse(any(), any())).willAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            return new CommentResponse(c.getId(), c.getContent(), null, null);
        });

        PageResponse<CommentResponse> response = commentService.getCommentList(postId, 1);

        assertThat(response.content()).hasSize(2);
        assertThat(response.content().get(0).content()).isEqualTo("Content1");
        assertThat(response.content().get(1).content()).isEqualTo("Content2");
    }

    @Test
    @DisplayName("getCommentList: 작성자 정보가 없으면 UserNotFoundException 발생")
    void getCommentList_userNotFound_throws() {
        Long postId = 10L;
        Post post = newPost(postId);
        User user1 = newUser(1L);
        Comment c1 = newComment(100L, post, user1, "Content1");

        Page<Comment> commentPage = new PageImpl<>(List.of(c1));

        given(postRepository.findByIdOrThrow(postId)).willReturn(post);
        given(commentRepository.findByPost(any(Post.class), any(PageRequest.class))).willReturn(commentPage);

        given(userRepository.findAllByIdIn(anySet())).willReturn(Collections.emptyList());

        assertThatThrownBy(() -> commentService.getCommentList(postId, 1))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("updateComment: 작성자 본인이면 댓글을 수정한다")
    void updateComment_success() {
        Long commentId = 100L;
        Long userId = 1L;
        CreateCommentRequest request = new CreateCommentRequest("Updated Content");

        User user = newUser(userId);
        Post post = newPost(10L);
        Comment comment = newComment(commentId, post, user, "Old Content");

        given(commentRepository.findByIdOrThrow(commentId)).willReturn(comment);
        given(userRepository.findByIdOrThrow(userId)).willReturn(user);

        CommentResponse expectedResponse = new CommentResponse(commentId, "Updated Content", null, null);
        given(commentMapper.toCommentResponse(comment, user)).willReturn(expectedResponse);

        CommentResponse response = commentService.updateComment(commentId, userId, request);

        assertThat(response.content()).isEqualTo("Updated Content");

        assertThat(comment.getContent()).isEqualTo("Updated Content");
    }

    @Test
    @DisplayName("updateComment: 작성자가 아니면 AUTH_FORBIDDEN 예외 발생")
    void updateComment_forbidden_throws() {
        Long commentId = 100L;
        Long ownerId = 1L;
        Long otherId = 2L;

        User owner = newUser(ownerId);
        User otherUser = newUser(otherId);
        Comment comment = newComment(commentId, newPost(10L), owner, "Old Content");

        given(commentRepository.findByIdOrThrow(commentId)).willReturn(comment);
        given(userRepository.findByIdOrThrow(otherId)).willReturn(otherUser);

        Throwable thrown = catchThrowable(() -> commentService.updateComment(commentId, otherId, new CreateCommentRequest("New")));

        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_FORBIDDEN);

        assertThat(comment.getContent()).isEqualTo("Old Content");
    }

    @Test
    @DisplayName("deleteComment: 작성자 본인이면 댓글을 삭제하고 카운트를 감소시킨다")
    void deleteComment_success() {
        Long commentId = 100L;
        Long userId = 1L;
        Long postId = 10L;

        User user = newUser(userId);
        Post post = newPost(postId);
        Comment comment = newComment(commentId, post, user, "Content");

        given(commentRepository.findByIdOrThrow(commentId)).willReturn(comment);
        given(userRepository.findByIdOrThrow(userId)).willReturn(user);
        given(postRepository.findByIdOrThrow(postId)).willReturn(post);

        commentService.deleteComment(commentId, userId);

        assertThat(comment.getDeletedAt()).isNotNull();

        verify(postCommentCounter).decreaseCommentCount(postId);
    }

    @Test
    @DisplayName("deleteComment: 작성자가 아니면 AUTH_FORBIDDEN 예외 발생")
    void deleteComment_forbidden_throws() {
        Long commentId = 100L;
        Long ownerId = 1L;
        Long otherId = 2L;
        Long postId = 10L;

        User owner = newUser(ownerId);
        User otherUser = newUser(otherId);
        Post post = newPost(postId);
        Comment comment = newComment(commentId, post, owner, "Content");

        given(commentRepository.findByIdOrThrow(commentId)).willReturn(comment);
        given(userRepository.findByIdOrThrow(otherId)).willReturn(otherUser);


        Throwable thrown = catchThrowable(() -> commentService.deleteComment(commentId, otherId));

        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_FORBIDDEN);

        assertThat(comment.getDeletedAt()).isNull();
        verify(postCommentCounter, never()).decreaseCommentCount(any());
    }
}