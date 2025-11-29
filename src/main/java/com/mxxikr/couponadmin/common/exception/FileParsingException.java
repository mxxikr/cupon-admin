package com.mxxikr.couponadmin.common.exception;

/**
 * 파일 파싱 중 발생하는 예외를 위한 커스텀 예외 클래스
 * BusinessException을 상속하여 일관된 예외 처리
 */
public class FileParsingException extends BusinessException {
    
    public FileParsingException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public FileParsingException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}

