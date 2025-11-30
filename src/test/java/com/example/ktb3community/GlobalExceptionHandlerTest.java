package com.example.ktb3community;

import com.example.ktb3community.common.error.ErrorCode;
import com.example.ktb3community.exception.CustomException;
import com.example.ktb3community.exception.GlobalExceptionHandler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.validation.annotation.Validated;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, GlobalExceptionHandlerTest.TestController.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    @RequestMapping("/test")
    @Validated
    public static class TestController {

        @GetMapping("/custom")
        void throwCustom() {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        @GetMapping("/authentication")
        void throwAuthentication() {
            throw new AuthenticationException("Auth failed") {};
        }

        @GetMapping("/access-denied")
        void throwAccessDenied() {
            throw new AccessDeniedException("Access denied");
        }

        @GetMapping("/data-integrity")
        void throwDataIntegrity() {
            throw new DataIntegrityViolationException("Constraint violation");
        }

        @GetMapping("/optimistic-lock")
        void throwOptimisticLock() {
            throw new ObjectOptimisticLockingFailureException(Object.class, "1");
        }

        @PostMapping("/bind")
        void throwBind(@Valid @ModelAttribute BindDto dto) {
        }

        @GetMapping("/runtime")
        void throwRuntime() {
            throw new RuntimeException("Unexpected error");
        }

        @GetMapping("/no-handler")
        void throwNoHandler() throws NoHandlerFoundException {
            throw new NoHandlerFoundException("GET", "/test/no-handler", null);
        }

        public static class BindDto {
            @NotBlank
            public String content;
        }
    }

    @Test
    @DisplayName("[CustomException] 커스텀 예외 발생 시 지정된 상태 코드와 에러 코드를 반환한다")
    void handleCustomException() throws Exception {
        mockMvc.perform(get("/test/custom"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("[422] @Valid 검증 실패 시 UNPROCESSABLE_ENTITY 반환")
    void handleMethodArgumentNotValid() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()));
    }

    @Test
    @DisplayName("[400] 파라미터 타입 불일치 시 BAD_REQUEST 반환")
    void handleMethodArgumentTypeMismatch() throws Exception {
        mockMvc.perform(get("/test/type-mismatch")
                        .param("id", "invalid-long"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_PARAMETER_TYPE.getCode()));
    }

    @Test
    @DisplayName("[422] 제약 조건 위반(ConstraintViolationException) 시 UNPROCESSABLE_ENTITY 반환")
    void handleConstraintViolation() throws Exception {
        mockMvc.perform(get("/test/constraint-violation")
                        .param("value", "   "))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()));
    }

    @Test
    @DisplayName("[400] 필수 파라미터 누락 시 BAD_REQUEST 반환")
    void handleMissingServletRequestParameter() throws Exception {
        mockMvc.perform(get("/test/missing-param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.MISSING_PARAMETER.getCode()));
    }

    @Test
    @DisplayName("[400] JSON 형식이 잘못된 경우(Body 읽기 실패) BAD_REQUEST 반환")
    void handleHttpMessageNotReadable() throws Exception {
        String malformedJson = "{ \"field\": \"value\"";

        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_REQUEST_BODY.getCode()));
    }

    @Test
    @DisplayName("[405] 지원하지 않는 HTTP 메서드 요청 시 METHOD_NOT_ALLOWED 반환")
    void handleHttpRequestMethodNotSupported() throws Exception {
        mockMvc.perform(delete("/test/custom"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value(ErrorCode.METHOD_NOT_ALLOWED.getCode()));
    }

    @Test
    @DisplayName("[401] 인증 예외(AuthenticationException) 발생 시 UNAUTHORIZED 반환")
    void handleAuthentication() throws Exception {
        mockMvc.perform(get("/test/authentication"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTH_UNAUTHORIZED.getCode()));
    }

    @Test
    @DisplayName("[403] 인가 예외(AccessDeniedException) 발생 시 FORBIDDEN 반환")
    void handleAccessDenied() throws Exception {
        mockMvc.perform(get("/test/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTH_FORBIDDEN.getCode()));
    }

    @Test
    @DisplayName("[409] 데이터 무결성 위반 시 CONFLICT 반환")
    void handleDataIntegrity() throws Exception {
        mockMvc.perform(get("/test/data-integrity"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.CONFLICT.getCode()));
    }

    @Test
    @DisplayName("[401] 낙관적 락 충돌 시 UNAUTHORIZED 반환")
    void handleOptimisticLock() throws Exception {
        mockMvc.perform(get("/test/optimistic-lock"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_REFRESH_TOKEN.getCode()));
    }

    @Test
    @DisplayName("[404] 핸들러를 찾을 수 없을 때(NoHandlerFoundException) NOT_FOUND 반환")
    void handleNoHandlerFound() throws Exception {
        mockMvc.perform(get("/test/no-handler"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("[500] 알 수 없는 예외 발생 시 INTERNAL_SERVER_ERROR 반환")
    void handleUnknown() throws Exception {
        mockMvc.perform(get("/test/runtime"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(ErrorCode.INTERNAL_SERVER_ERROR.getCode()));
    }
}
