package com.example.ktb3community.post.service;

import com.example.ktb3community.common.pagination.PageResponse;
import com.example.ktb3community.post.PostSort;
import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.dto.Author;
import com.example.ktb3community.post.dto.CreatePostRequest;
import com.example.ktb3community.post.dto.CreatePostResponse;
import com.example.ktb3community.post.dto.PostListResponse;
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

    public CreatePostResponse createPost(CreatePostRequest createPostRequest) {
        if(!inMemoryUserRepository.existsById(createPostRequest.userId())){
            throw new UserNotFoundException();
        }
        Post saved = inMemoryPostRepository.save(Post.createNew(createPostRequest.userId(), createPostRequest.title(),
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

}
