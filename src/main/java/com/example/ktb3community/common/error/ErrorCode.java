package com.example.ktb3community.common.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Common
    INVALID_INPUT_VALUE(HttpStatus.UNPROCESSABLE_ENTITY, "REQUEST_VALIDATION_ERROR", "요청 값이 유효하지 않습니다."),
    INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_BODY", "요청 본문을 읽을 수 없습니다."),
    INVALID_PARAMETER_TYPE(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER_TYPE", "요청 파라미터 타입이 올바르지 않습니다."),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER", "필수 요청 파라미터가 누락되었습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "지원하지 않는 HTTP 메서드입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "리소스를 찾을 수 없습니다."),
    CONFLICT(HttpStatus.CONFLICT, "CONFLICT", "요청이 현재 리소스 상태와 충돌합니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."),

    // Auth
    AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_UNAUTHORIZED", "인증이 필요합니다."),
    AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_FORBIDDEN", "접근 권한이 없습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "토큰이 만료되었습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "유효하지 않은 리프레시 토큰입니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_ACCESS_TOKEN", "유효하지 않은 액세스 토큰입니다."),
    NOT_EXIST_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "NOT_EXIST_REFRESH_TOKEN", "리프레시 토큰이 존재하지 않습니다."),
    INVALID_TOKEN_REUSE_DETECTED(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN_REUSE_DETECTED", "리프레시 토큰 재사용이 감지되었습니다. 모든 토큰이 무효화됩니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_EXPIRED"," 리프레시 토큰이 만료되었습니다."),
    INVALID_USER_ID(HttpStatus.BAD_REQUEST, "INVALID_USER_ID", "유효하지 않은 사용자 ID입니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "존재하지 않는 사용자입니다."),
    EMAIL_ALREADY_EXIST(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXIST", "이미 사용 중인 이메일입니다."),
    NICKNAME_ALREADY_EXIST(HttpStatus.CONFLICT, "NICKNAME_ALREADY_EXIST", "이미 사용 중인 닉네임입니다."),

    //POST
    INVALID_SORT(HttpStatus.BAD_REQUEST, "INVALID_SORT", "sort는 new|view|like|cmt만 허용합니다."),
    INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST, "INVALID_PAGE_SIZE", "pageSize는 1~20 사이만 허용합니다."),
    INVALID_PAGE(HttpStatus.BAD_REQUEST, "INVALID_PAGE", "page는 1부터 허용합니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_NOT_FOUND", "존재하지 않는 게시글입니다."),
    INVALID_CURSOR_ID(HttpStatus.BAD_REQUEST, "INVALID_CURSOR_ID", "유효하지 않은 커서 ID입니다."),

    //COMMENT
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND", "존재하지 않는 댓글입니다."),


    //S3
    S3_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3_UPLOAD_FAILED", "파일 업로드에 실패했습니다."),
    S3_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3_DELETE_FAILED", "파일 삭제에 실패했습니다."),
    INVALID_IMG_URL(HttpStatus.INTERNAL_SERVER_ERROR, "INVALID_IMG_URL", "이미지 url이 올바르지 않습니다."),
    S3_DOWNLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3_DOWNLOAD_FAILED", "파일 다운로드에 실패했습니다."),
    FILE_NAME_IS_NOT_BLANK(HttpStatus.BAD_REQUEST, "FILE_NAME_IS_NOT_BLANK", "파일 이름은 공백일 수 없습니다."),
    CONTENT_TYPE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "CONTENT_TYPE_NOT_ALLOWED", "허용되지 않는 컨텐츠 타입입니다."),
    INVALID_S3_KEY(HttpStatus.BAD_REQUEST, "INVALID_S3_KEY", "유효하지 않은 S3 키입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
