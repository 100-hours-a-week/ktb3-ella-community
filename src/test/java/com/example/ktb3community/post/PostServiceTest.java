package com.example.ktb3community.post;

import com.example.ktb3community.comment.repository.CommentRepository;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.dto.CreatePostRequest;
import com.example.ktb3community.post.dto.CreatePostResponse;
import com.example.ktb3community.post.repository.PostRepository;
import com.example.ktb3community.post.service.PostService;
import com.example.ktb3community.s3.service.FileService;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static com.example.ktb3community.TestFixtures.POST_ID;
import static com.example.ktb3community.TestFixtures.USER_ID;
import static com.example.ktb3community.TestEntityFactory.post;
import static com.example.ktb3community.TestEntityFactory.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock UserRepository userRepository;
    @Mock PostRepository postRepository;
    @Mock CommentRepository commentRepository;
    @Mock FileService fileService;

    @InjectMocks
    PostService postService;

    @Test
    @DisplayName("createPost: 게시글이 정상적으로 저장되고 ID를 반환한다")
    void createPost_success() {
        CreatePostRequest request = new CreatePostRequest("Title", "Content", "http://image.url");
        User user = user().id(USER_ID).build();

        given(userRepository.findByIdOrThrow(USER_ID)).willReturn(user);

        given(postRepository.save(any(Post.class))).willAnswer(invocation -> {
            Post p = invocation.getArgument(0);
            ReflectionTestUtils.setField(p, "id", POST_ID);
            return p;
        });

        CreatePostResponse response = postService.createPost(USER_ID, request);

        assertThat(response.postId()).isEqualTo(POST_ID);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());

        Post capturedPost = postCaptor.getValue();
        assertThat(capturedPost.getUser()).isEqualTo(user);
        assertThat(capturedPost.getTitle()).isEqualTo("Title");
        assertThat(capturedPost.getContent()).isEqualTo("Content");
    }

    @Test
    @DisplayName("updatePost: 작성자 본인이면 게시글을 수정하고, 기존 이미지가 변경되면 파일 삭제 로직을 호출한다")
    void updatePost_success() {
        CreatePostRequest request = new CreatePostRequest("New Title", "New Content", "http://new-image.com");
        User user = user().id(USER_ID).build();
        Post post = post(user)
                .id(POST_ID)
                .title("Old Title")
                .content("Old Content")
                .postImageUrl("http://old-image.com")
                .build();

        given(userRepository.findByIdOrThrow(USER_ID)).willReturn(user);
        given(postRepository.findByIdOrThrow(POST_ID)).willReturn(post);

        CreatePostResponse response = postService.updatePost(POST_ID, USER_ID, request);

        assertThat(response.postId()).isEqualTo(POST_ID);

        assertThat(post.getTitle()).isEqualTo("New Title");
        assertThat(post.getContent()).isEqualTo("New Content");
        assertThat(post.getPostImageUrl()).isEqualTo("http://new-image.com");

        verify(fileService).deleteImageIfChanged("http://old-image.com", "http://new-image.com");
    }

    @Test
    @DisplayName("updatePost: 작성자가 아니면 AUTH_FORBIDDEN 예외가 발생한다")
    void updatePost_notOwner_throws() {
        CreatePostRequest request = new CreatePostRequest("Title", "Content", "Img");
        User owner = user().id(USER_ID).build(); // ID: 1L
        Post post = post(owner)
                .id(POST_ID)
                .title("Old Title")
                .content("Old Content")
                .postImageUrl("http://old-image.com")
                .build();

        Long otherUserId = 999L; // 다른 유저 ID

        given(userRepository.findByIdOrThrow(otherUserId)).willReturn(user().id(otherUserId).build());
        given(postRepository.findByIdOrThrow(POST_ID)).willReturn(post);

        Throwable thrown = catchThrowable(() -> postService.updatePost(POST_ID, otherUserId, request));

        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_FORBIDDEN);

        assertThat(post.getTitle()).isEqualTo("Old Title");
    }

    @Test
    @DisplayName("deletePost: 작성자 본인이면 댓글과 게시글을 소프트 삭제한다")
    void deletePost_success() {
        User user = user().id(USER_ID).build();
        Post post = post(user).id(POST_ID).build();

        given(userRepository.findByIdOrThrow(USER_ID)).willReturn(user);
        given(postRepository.findByIdOrThrow(POST_ID)).willReturn(post);

        postService.deletePost(POST_ID, USER_ID);

        verify(commentRepository).softDeleteByPostId(any(Long.class), any(Instant.class));

        assertThat(post.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("deletePost: 작성자가 아니면 AUTH_FORBIDDEN 예외가 발생한다")
    void deletePost_notOwner_throws() {
        User owner = user().id(USER_ID).build(); // ID: 1L
        Post post = post(owner).id(POST_ID).build();
        Long otherUserId = 999L;

        given(userRepository.findByIdOrThrow(otherUserId)).willReturn(user().id(otherUserId).build());
        given(postRepository.findByIdOrThrow(POST_ID)).willReturn(post);

        Throwable thrown = catchThrowable(() -> postService.deletePost(POST_ID, otherUserId));

        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_FORBIDDEN);
        verify(commentRepository, times(0)).softDeleteByPostId(any(), any());
        assertThat(post.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("increaseCommentCount: 게시글의 댓글 카운트를 1 증가시킨다")
    void increaseCommentCount_success() {
        Post post = post(user().id(USER_ID).build()).id(POST_ID).build();
        given(postRepository.findByIdOrThrow(POST_ID)).willReturn(post);

        postService.increaseCommentCount(POST_ID);

        assertThat(post.getCommentCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("decreaseCommentCount: 게시글의 댓글 카운트를 1 감소시킨다")
    void decreaseCommentCount_success() {
        Post post = post(user().id(USER_ID).build()).id(POST_ID).build();
        post.increaseCommentCount();
        given(postRepository.findByIdOrThrow(POST_ID)).willReturn(post);

        postService.decreaseCommentCount(POST_ID);

        assertThat(post.getCommentCount()).isZero();
    }
}
