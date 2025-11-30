package com.example.ktb3community.post;

import com.example.ktb3community.comment.dto.CommentResponse;
import com.example.ktb3community.comment.service.CommentService;
import com.example.ktb3community.common.pagination.PageResponse;
import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.dto.PostDetailResponse;
import com.example.ktb3community.post.dto.PostListResponse;
import com.example.ktb3community.post.repository.PostLikeRepository;
import com.example.ktb3community.post.repository.PostRepository;
import com.example.ktb3community.post.service.PostViewService;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.exception.UserNotFoundException;
import com.example.ktb3community.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.example.ktb3community.TestEntityFactory.post;
import static com.example.ktb3community.TestEntityFactory.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
@ExtendWith(MockitoExtension.class)
class PostViewServiceTest {

    @Mock PostRepository postRepository;
    @Mock UserRepository userRepository;
    @Mock PostLikeRepository postLikeRepository;
    @Mock CommentService commentService;

    @InjectMocks
    PostViewService postViewService;

    @Test
    @DisplayName("getPostList: 게시글 목록과 작성자 정보를 정상적으로 매핑하여 반환한다")
    void getPostList_success() {
        User user1 = user().id(1L).nickname("user1").profileImageUrl("img").build();
        User user2 = user().id(2L).nickname("user2").profileImageUrl("img").build();

        Post post1 = post(user1).id(10L).title("Title1").content("Content").build();
        Post post2 = post(user2).id(11L).title("Title2").content("Content").build();

        List<Post> posts = List.of(post1, post2);
        Page<Post> postPage = new PageImpl<>(posts, PageRequest.of(0, 10), posts.size());

        PostSort sort = mock(PostSort.class);
        given(sort.sort()).willReturn(Sort.unsorted());

        given(postRepository.findAll(any(PageRequest.class))).willReturn(postPage);
        given(userRepository.findAllByIdIn(Set.of(1L, 2L))).willReturn(List.of(user1, user2));

        PageResponse<PostListResponse> response = postViewService.getPostList(1, 10, sort);

        assertThat(response.content()).hasSize(2);

        PostListResponse res1 = response.content().getFirst();
        assertThat(res1.title()).isEqualTo("Title1");
        assertThat(res1.author().nickname()).isEqualTo("user1");

        PostListResponse res2 = response.content().get(1);
        assertThat(res2.title()).isEqualTo("Title2");
        assertThat(res2.author().nickname()).isEqualTo("user2");
    }

    @Test
    @DisplayName("getPostList: 작성자 정보가 DB에 없으면 UserNotFoundException 발생")
    void getPostList_authorNotFound_throws() {

        User user1 = user().id(1L).nickname("user1").profileImageUrl("img").build();
        Post post1 = post(user1).id(10L).title("Title1").content("Content").build();

        Page<Post> postPage = new PageImpl<>(List.of(post1));
        PostSort sort = mock(PostSort.class);
        given(sort.sort()).willReturn(Sort.unsorted());

        given(postRepository.findAll(any(PageRequest.class))).willReturn(postPage);
        given(userRepository.findAllByIdIn(anySet())).willReturn(Collections.emptyList());

        assertThatThrownBy(() -> postViewService.getPostList(1, 10, sort))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("getPostDetail: 상세 조회 시 조회수가 1 증가하고 상세 정보를 반환한다")
    void getPostDetail_success() {
        Long postId = 10L;
        Long viewerId = 99L;
        Long authorId = 1L;

        User author = user().id(authorId).nickname("author").profileImageUrl("img").build();
        User viewer = user().id(viewerId).nickname("viewer").profileImageUrl("img").build();
        Post post = post(author).id(postId).title("Detail Title").content("Content").build();

        given(postRepository.findByIdOrThrow(postId)).willReturn(post);
        given(userRepository.findByIdOrThrow(authorId)).willReturn(author);
        given(userRepository.findByIdOrThrow(viewerId)).willReturn(viewer);

        given(postLikeRepository.exists(post, viewer)).willReturn(true);

        PageResponse<CommentResponse> emptyComments = new PageResponse<>(Collections.emptyList(), 1, 10, 0);
        given(commentService.getCommentList(postId, 1)).willReturn(emptyComments);

        PostDetailResponse response = postViewService.getPostDetail(postId, viewerId);

        assertThat(post.getViewCount()).isEqualTo(1);

        assertThat(response.postId()).isEqualTo(postId);
        assertThat(response.title()).isEqualTo("Detail Title");
        assertThat(response.author().nickname()).isEqualTo("author");
        assertThat(response.liked()).isTrue();
        assertThat(response.comments()).isSameAs(emptyComments);
    }
}
