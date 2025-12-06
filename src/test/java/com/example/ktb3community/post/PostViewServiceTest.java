package com.example.ktb3community.post;

import com.example.ktb3community.comment.dto.CommentResponse;
import com.example.ktb3community.comment.service.CommentService;
import com.example.ktb3community.common.pagination.CursorResponse;
import com.example.ktb3community.common.pagination.PageResponse;
import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.dto.PostDetailResponse;
import com.example.ktb3community.post.dto.PostListResponse;
import com.example.ktb3community.post.repository.PostLikeRepository;
import com.example.ktb3community.post.repository.PostRepository;
import com.example.ktb3community.post.service.PostViewService;
import com.example.ktb3community.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static com.example.ktb3community.TestEntityFactory.post;
import static com.example.ktb3community.TestEntityFactory.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostViewServiceTest {

    @Mock PostRepository postRepository;
    @Mock PostLikeRepository postLikeRepository;
    @Mock CommentService commentService;

    @InjectMocks
    PostViewService postViewService;

    @Test
    @DisplayName("getPostList: 게시글 목록과 작성자 정보를 정상적으로 매핑하여 반환한다 (hasNext = false)")
    void getPostList_success() {
        User user1 = user().id(1L).nickname("user1").profileImageUrl("img1").build();
        User user2 = user().id(2L).nickname("user2").profileImageUrl("img2").build();

        Post post1 = post(user1).id(10L).title("Title1").content("Content1").build();
        Post post2 = post(user2).id(11L).title("Title2").content("Content2").build();

        List<Post> posts = List.of(post1, post2);

        PostSort sort = mock(PostSort.class);
        given(sort.usesCursorValue()).willReturn(false);

        given(postRepository.findAllByCursor(any())).willReturn(posts);

        int size = 2;

        CursorResponse<PostListResponse> response =
                postViewService.getPostList(null, null, size, sort);

        assertThat(response.content()).hasSize(2);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursorId()).isEqualTo(11L);
        assertThat(response.nextCursorValue()).isNull();

        PostListResponse res1 = response.content().getFirst();
        assertThat(res1.postId()).isEqualTo(10L);
        assertThat(res1.title()).isEqualTo("Title1");
        assertThat(res1.author().nickname()).isEqualTo("user1");
        assertThat(res1.author().profileImageUrl()).isEqualTo("img1");

        PostListResponse res2 = response.content().get(1);
        assertThat(res2.postId()).isEqualTo(11L);
        assertThat(res2.title()).isEqualTo("Title2");
        assertThat(res2.author().nickname()).isEqualTo("user2");
        assertThat(res2.author().profileImageUrl()).isEqualTo("img2");
    }

    @Test
    @DisplayName("getPostList: size+1 개를 조회하면 hasNext=true, 마지막 요소 기준으로 cursor가 설정된다")
    void getPostList_hasNext_andCursor() {
        User user = user().id(1L).nickname("user").profileImageUrl("img").build();

        Post p1 = post(user).id(10L).title("T1").build();
        Post p2 = post(user).id(11L).title("T2").build();
        Post p3 = post(user).id(12L).title("T3").build();

        List<Post> posts = List.of(p1, p2, p3);

        PostSort sort = mock(PostSort.class);
        given(sort.usesCursorValue()).willReturn(true);
        given(sort.extractKey(any(Post.class))).willReturn(999L);

        given(postRepository.findAllByCursor(any())).willReturn(posts);

        int size = 2;

        CursorResponse<PostListResponse> response =
                postViewService.getPostList(null, null, size, sort);

        assertThat(response.content()).hasSize(2);
        assertThat(response.hasNext()).isTrue();

        assertThat(response.nextCursorId()).isEqualTo(11L);
        assertThat(response.nextCursorValue()).isEqualTo(999L);
    }

    @Test
    @DisplayName("getPostDetail: 상세 조회 시 조회수가 1 증가하고 상세 정보를 반환한다")
    void getPostDetail_success() {
        Long postId = 10L;
        long viewerId = 99L;

        User author = user().id(1L).nickname("author").profileImageUrl("authorImg").build();
        Post post = post(author)
                .id(postId)
                .title("Detail Title")
                .content("Content")
                .viewCount(0L)
                .build();

        int beforeViewCount = Math.toIntExact(post.getViewCount());

        given(postRepository.findByIdOrThrow(postId)).willReturn(post);
        given(postLikeRepository.exists(postId, viewerId)).willReturn(true);

        PageResponse<CommentResponse> emptyComments =
                new PageResponse<>(Collections.emptyList(), 1, 10, 0);
        given(commentService.getCommentList(postId, 1)).willReturn(emptyComments);

        PostDetailResponse response = postViewService.getPostDetail(postId, viewerId);

        assertThat(post.getViewCount()).isEqualTo(beforeViewCount + 1);

        assertThat(response.postId()).isEqualTo(postId);
        assertThat(response.title()).isEqualTo("Detail Title");
        assertThat(response.author().nickname()).isEqualTo("author");
        assertThat(response.author().profileImageUrl()).isEqualTo("authorImg");
        assertThat(response.liked()).isTrue();
        assertThat(response.comments()).isSameAs(emptyComments);

        verify(postRepository).findByIdOrThrow(postId);
        verify(postLikeRepository).exists(postId, viewerId);
        verify(commentService).getCommentList(postId, 1);
    }
}
