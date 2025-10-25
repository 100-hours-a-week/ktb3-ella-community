package com.example.ktb3community.user.mapper;

import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.dto.MeResponse;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {
    MeResponse userToMeResponse(User user);
}
