package com.example.ktb3community.auth.security;

import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.user.domain.User;
import com.example.ktb3community.user.exception.UserNotFoundException;
import com.example.ktb3community.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // 인증 과정에서 이 메서드를 호출해서 UserDetails를 로드함
    // username 파라미터에는 JWT에서 추출한 userId가 문자열로 들어옴
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        long userId;
        try {
            userId = Long.parseLong(username);
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.INVALID_USER_ID);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        return CustomUserDetails.from(user);
    }
}


