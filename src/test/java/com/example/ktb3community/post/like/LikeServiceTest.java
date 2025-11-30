package com.example.ktb3community.post.like;

import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.dto.LikeResponse;
import com.example.ktb3community.post.repository.PostLikeRepository;
import com.example.ktb3community.post.repository.PostRepository;
import com.example.ktb3community.post.service.LikeService;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.example.ktb3community.TestFixtures.POST_ID;
import static com.example.ktb3community.TestFixtures.USER_ID;
import static com.example.ktb3community.TestEntityFactory.post;
import static com.example.ktb3community.TestEntityFactory.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock UserRepository userRepository;
    @Mock PostRepository postRepository;
    @Mock PostLikeRepository postLikeRepository;

    @InjectMocks
    LikeService likeService;

    @Test
    @DisplayName("likePost: 좋아요를 처음 누르면 카운트가 1 증가한다")
    void likePost_added_increasesCount() {
        User user = user().id(USER_ID).build();
        Post post = post().id(POST_ID).likeCount(10).viewCount(5).commentCount(3).build();
        long initialCount = post.getLikeCount();

        given(userRepository.findByIdOrThrow(USER_ID)).willReturn(user);
        given(postRepository.findByIdOrThrow(POST_ID)).willReturn(post);
        given(postLikeRepository.add(post, user)).willReturn(true);

        LikeResponse response = likeService.likePost(POST_ID, USER_ID);

        assertThat(post.getLikeCount()).isEqualTo(initialCount + 1);
        assertThat(response.likeCount()).isEqualTo(initialCount + 1);

        verify(postLikeRepository).add(post, user);
    }

    @Test
    @DisplayName("likePost: 이미 좋아요를 눌렀던 경우 카운트는 증가하지 않는다")
    void likePost_alreadyLiked_doesNotIncreaseCount() {
        User user = user().id(USER_ID).build();
        Post post = post().id(POST_ID).likeCount(10).viewCount(5).commentCount(3).build();
        long initialCount = post.getLikeCount();

        given(userRepository.findByIdOrThrow(USER_ID)).willReturn(user);
        given(postRepository.findByIdOrThrow(POST_ID)).willReturn(post);
        given(postLikeRepository.add(post, user)).willReturn(false);

        LikeResponse response = likeService.likePost(POST_ID, USER_ID);

        assertThat(post.getLikeCount()).isEqualTo(initialCount);
        assertThat(response.likeCount()).isEqualTo(initialCount);
    }

    @Test
    @DisplayName("unlikePost: 좋아요 취소 성공 시 카운트가 1 감소한다")
    void unlikePost_removed_decreasesCount() {
        User user = user().id(USER_ID).build();
        Post post = post().id(POST_ID).likeCount(10).viewCount(5).commentCount(3).build();
        long initialCount = post.getLikeCount();

        given(userRepository.findByIdOrThrow(USER_ID)).willReturn(user);
        given(postRepository.findByIdOrThrow(POST_ID)).willReturn(post);
        given(postLikeRepository.remove(post, user)).willReturn(true);

        LikeResponse response = likeService.unlikePost(POST_ID, USER_ID);

        assertThat(post.getLikeCount()).isEqualTo(initialCount - 1);
        assertThat(response.likeCount()).isEqualTo(initialCount - 1);

        verify(postLikeRepository).remove(post, user);
    }

    @Test
    @DisplayName("unlikePost: 좋아요를 누른 적이 없는 경우카운트는 감소하지 않는다")
    void unlikePost_notLiked_doesNotDecreaseCount() {
        User user = user().id(USER_ID).build();
        Post post = post().id(POST_ID).likeCount(10).viewCount(5).commentCount(3).build();
        long initialCount = post.getLikeCount();

        given(userRepository.findByIdOrThrow(USER_ID)).willReturn(user);
        given(postRepository.findByIdOrThrow(POST_ID)).willReturn(post);
        given(postLikeRepository.remove(post, user)).willReturn(false);

        LikeResponse response = likeService.unlikePost(POST_ID, USER_ID);

        assertThat(post.getLikeCount()).isEqualTo(initialCount);
        assertThat(response.likeCount()).isEqualTo(initialCount);
    }
}
