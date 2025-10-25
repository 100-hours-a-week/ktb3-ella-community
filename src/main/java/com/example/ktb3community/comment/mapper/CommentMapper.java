package com.example.ktb3community.comment.mapper;

import com.example.ktb3community.comment.domain.Comment;
import com.example.ktb3community.comment.dto.CommentResponse;
import com.example.ktb3community.post.dto.Author;
import com.example.ktb3community.user.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface CommentMapper {
    Author userToAuthor(User user);

    @Mapping(target = "commentId", source = "comment.id")
    @Mapping(target = "author", source = "user")
    @Mapping(target = "createdAt", source = "comment.createdAt")
    CommentResponse toCommentResponse(Comment comment, User user);
}
