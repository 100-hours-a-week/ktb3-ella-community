package com.example.ktb3community.post;

import com.example.ktb3community.post.domain.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static com.example.ktb3community.TestEntityFactory.post;
import static org.assertj.core.api.Assertions.assertThat;

class PostSortTest {

    @Test
    @DisplayName("extractKey: 각 정렬 기준에 맞는 값을 추출한다")
    void extractKey_bySortType() {
        Post p = post().build();
        ReflectionTestUtils.setField(p, "id", 10L);
        ReflectionTestUtils.setField(p, "viewCount", 20L);
        ReflectionTestUtils.setField(p, "likeCount", 30L);
        ReflectionTestUtils.setField(p, "commentCount", 40L);

        assertThat(PostSort.LATEST.extractKey(p)).isEqualTo(10L);
        assertThat(PostSort.VIEW.extractKey(p)).isEqualTo(20L);
        assertThat(PostSort.LIKE.extractKey(p)).isEqualTo(30L);
        assertThat(PostSort.CMT.extractKey(p)).isEqualTo(40L);
    }

    @Test
    @DisplayName("usesCursorValue: LATEST만 false, 나머지는 true를 반환한다")
    void usesCursorValue_flag() {
        assertThat(PostSort.LATEST.usesCursorValue()).isFalse();
        assertThat(PostSort.VIEW.usesCursorValue()).isTrue();
        assertThat(PostSort.LIKE.usesCursorValue()).isTrue();
        assertThat(PostSort.CMT.usesCursorValue()).isTrue();
    }

    @Test
    @DisplayName("descendingComparator(LATEST): ID 기준 내림차순, ID가 tie-breaker로 사용된다")
    void descendingComparator_latest() {
        Post p1 = post().build();
        ReflectionTestUtils.setField(p1, "id", 1L);

        Post p2 = post().build();
        ReflectionTestUtils.setField(p2, "id", 3L);

        Post p3 = post().build();
        ReflectionTestUtils.setField(p3, "id", 2L);

        List<Post> list = new ArrayList<>(List.of(p1, p2, p3));

        list.sort(PostSort.LATEST.descendingComparator());

        assertThat(list)
                .extracting(Post::getId)
                .containsExactly(3L, 2L, 1L);
    }

    @Test
    @DisplayName("descendingComparator: 조회수 내림차순, 조회수가 같으면 ID 내림차순으로 정렬된다")
    void descendingComparator_view() {
        Post p1 = post().build();
        ReflectionTestUtils.setField(p1, "id", 1L);
        ReflectionTestUtils.setField(p1, "viewCount", 10L);

        Post p2 = post().build();
        ReflectionTestUtils.setField(p2, "id", 2L);
        ReflectionTestUtils.setField(p2, "viewCount", 5L);

        Post p3 = post().build();
        ReflectionTestUtils.setField(p3, "id", 3L);
        ReflectionTestUtils.setField(p3, "viewCount", 10L); // p1과 같은 viewCount, 더 큰 ID

        List<Post> list = new ArrayList<>(List.of(p1, p2, p3));

        list.sort(PostSort.VIEW.descendingComparator());

        assertThat(list)
                .extracting(Post::getId)
                .containsExactly(3L, 1L, 2L);
    }

    @Test
    @DisplayName("descendingComparator(LIKE): 좋아요 수 내림차순, tie 시 ID 내림차순")
    void descendingComparator_like() {
        Post p1 = post().build();
        ReflectionTestUtils.setField(p1, "id", 1L);
        ReflectionTestUtils.setField(p1, "likeCount", 3L);

        Post p2 = post().build();
        ReflectionTestUtils.setField(p2, "id", 2L);
        ReflectionTestUtils.setField(p2, "likeCount", 7L);

        Post p3 = post().build();
        ReflectionTestUtils.setField(p3, "id", 3L);
        ReflectionTestUtils.setField(p3, "likeCount", 7L);

        List<Post> list = new ArrayList<>(List.of(p1, p2, p3));

        list.sort(PostSort.LIKE.descendingComparator());

        assertThat(list)
                .extracting(Post::getId)
                .containsExactly(3L, 2L, 1L);
    }

    @Test
    @DisplayName("descendingComparator(CMT): 댓글 수 내림차순, tie 시 ID 내림차순")
    void descendingComparator_comment() {
        Post p1 = post().build();
        ReflectionTestUtils.setField(p1, "id", 1L);
        ReflectionTestUtils.setField(p1, "commentCount", 0L);

        Post p2 = post().build();
        ReflectionTestUtils.setField(p2, "id", 2L);
        ReflectionTestUtils.setField(p2, "commentCount", 5L);

        Post p3 = post().build();
        ReflectionTestUtils.setField(p3, "id", 3L);
        ReflectionTestUtils.setField(p3, "commentCount", 2L);

        List<Post> list = new ArrayList<>(List.of(p1, p2, p3));

        list.sort(PostSort.CMT.descendingComparator());

        assertThat(list)
                .extracting(Post::getId)
                .containsExactly(2L, 3L, 1L);
    }
}
