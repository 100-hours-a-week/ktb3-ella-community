package com.example.ktb3community.post.service;

import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.post.domain.Post;
import com.example.ktb3community.post.dto.CreatePostRequest;
import com.example.ktb3community.post.dto.CreatePostResponse;
import com.example.ktb3community.post.repository.InMemoryPostRepository;
import com.example.ktb3community.user.exception.UserNotFoundException;
import com.example.ktb3community.user.repository.InMemoryUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

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

}
