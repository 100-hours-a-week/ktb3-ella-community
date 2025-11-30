package com.example.ktb3community.s3;


import com.example.ktb3community.auth.security.CustomUserDetails;
import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.BusinessException;
import com.example.ktb3community.s3.controller.FileController;
import com.example.ktb3community.s3.dto.PresignUploadResponse;
import com.example.ktb3community.s3.service.FileService;
import com.example.ktb3community.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.example.ktb3community.TestFixtures.USER_ID;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileController.class)
class FileControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean FileService fileService;

    @BeforeEach
    void setUp() {
        User mockUser = User.builder()
                .id(USER_ID)
                .email("test@email.com")
                .role(com.example.ktb3community.common.Role.ROLE_USER)
                .build();

        CustomUserDetails principal = CustomUserDetails.from(mockUser);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("[200] Presigned URL 발급 성공")
    void presignUpload_200_defaultContentType() throws Exception {
        String fileName = "test.png";
        String defaultContentType = "image/png";

        Map<String, List<String>> headers = Map.of(
                "Content-Type", List.of("image/jpeg"),
                "x-amz-meta-author", List.of("tester")
        );

        PresignUploadResponse response = new PresignUploadResponse(
                "https://s3.url/presigned",
                "images/uuid_test.png",
                headers
        );

        given(fileService.presignUpload(fileName, defaultContentType)).willReturn(response);

        mockMvc.perform(get("/uploads/presigned-url")
                        .param("fileName", fileName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.uploadUrl").value("https://s3.url/presigned"))
                .andExpect(jsonPath("$.data.key").value("images/uuid_test.png"));

        verify(fileService).presignUpload(fileName, defaultContentType);
    }

    @Test
    @DisplayName("[200] Presigned URL 발급 성공")
    void presignUpload_200_customContentType() throws Exception {
        String fileName = "test.jpg";
        String contentType = "image/jpeg";

        PresignUploadResponse response = new PresignUploadResponse(
                "https://s3.url/presigned-jpg",
                "images/uuid_test.jpg",
                Collections.emptyMap()
        );

        given(fileService.presignUpload(fileName, contentType)).willReturn(response);

        mockMvc.perform(get("/uploads/presigned-url")
                        .param("fileName", fileName)
                        .param("contentType", contentType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.uploadUrl").value("https://s3.url/presigned-jpg"));

        verify(fileService).presignUpload(fileName, contentType);
    }

    @Test
    @DisplayName("[400] 파일 이름(fileName) 파라미터가 없으면 400 Bad Request")
    void presignUpload_400_missingParam() throws Exception {

        mockMvc.perform(get("/uploads/presigned-url")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[400] 지원하지 않는 파일 형식이면 예외 발생")
    void presignUpload_400_invalidType() throws Exception {
        String fileName = "test.txt";
        String invalidType = "text/plain";

        given(fileService.presignUpload(fileName, invalidType))
                .willThrow(new BusinessException(ErrorCode.CONTENT_TYPE_NOT_ALLOWED));

        mockMvc.perform(get("/uploads/presigned-url")
                        .param("fileName", fileName)
                        .param("contentType", invalidType))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.CONTENT_TYPE_NOT_ALLOWED.getCode()));
    }
}
