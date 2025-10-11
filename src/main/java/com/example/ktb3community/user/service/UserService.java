package com.example.ktb3community.user.service;


import com.example.ktb3community.user.dto.AvailabilityResponse;
import com.example.ktb3community.user.repository.InMemoryUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
    private final InMemoryUserRepository inMemoryUserRepository;

    public AvailabilityResponse getAvailability(String email, String nickname) {
        Boolean emailAvailable = null;
        Boolean nicknameAvailable = null;
        if(email != null && !email.isBlank()){
            email = email.trim().toLowerCase();
            emailAvailable = !inMemoryUserRepository.existsByEmail(email);
        }
        if(nickname != null && !nickname.isBlank()){
            nickname = nickname.trim();
            nicknameAvailable = !inMemoryUserRepository.existsByNickname(nickname);
        }
        return new AvailabilityResponse(emailAvailable, nicknameAvailable);
    }


}