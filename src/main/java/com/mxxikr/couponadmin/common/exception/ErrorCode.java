package com.mxxikr.couponadmin.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 애플리케이션에서 발생하는 모든 에러 코드를 정의하는 enum
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통 에러
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력 값입니다"),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "C002", "요청한 파일을 찾을 수 없습니다"),
    UNSUPPORTED_FILE_TYPE(HttpStatus.BAD_REQUEST, "C003", "지원하지 않는 파일 형식입니다. CSV 또는 Excel 파일만 지원합니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "내부 서버 오류가 발생했습니다"),

    // 파일 파싱 에러
    FILE_IS_EMPTY(HttpStatus.BAD_REQUEST, "F001", "파일이 비어있습니다"),
    INVALID_FILE_HEADER(HttpStatus.BAD_REQUEST, "F002", "유효하지 않은 헤더입니다. 첫 번째 컬럼은 'customer_id'여야 합니다"),
    EMPTY_CUSTOMER_LIST(HttpStatus.BAD_REQUEST, "F003", "고객 목록이 비어있습니다"),
    FILE_PARSING_FAILED(HttpStatus.BAD_REQUEST, "F004", "파일 파싱에 실패했습니다"),

    // 스토리지(파일 시스템) 에러
    DIRECTORY_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S001", "디렉토리 생성에 실패했습니다"),
    FILE_STORAGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S002", "파일 저장에 실패했습니다"),
    FILE_READ_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S003", "파일을 읽어오는 데 실패했습니다"),
    UNSUPPORTED_OPERATION(HttpStatus.BAD_REQUEST, "S004", "지원하지 않는 작업입니다"),
    INVALID_STORAGE_TYPE(HttpStatus.INTERNAL_SERVER_ERROR, "S005", "유효하지 않은 스토리지 타입입니다"),
    INVALID_FILE_PARSER(HttpStatus.INTERNAL_SERVER_ERROR, "S006", "파일 파서를 찾을 수 없습니다"),
    
    // 작업 상태 에러
    JOB_NOT_FOUND(HttpStatus.NOT_FOUND, "J001", "요청한 작업을 찾을 수 없습니다"),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}