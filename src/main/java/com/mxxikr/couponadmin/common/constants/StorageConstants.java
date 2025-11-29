package com.mxxikr.couponadmin.common.constants;

/**
 * 스토리지 관련 상수 정의
 */
public final class StorageConstants {
    
    private StorageConstants() {
        throw new AssertionError("상수 클래스는 인스턴스화할 수 없습니다");
    }
    
    /**
     * 기본 Presigned URL 만료 시간 (분)
     */
    public static final int DEFAULT_PRESIGNED_URL_EXPIRATION_MINUTES = 60;
    
    /**
     * LocalStack용 기본 자격 증명
     */
    public static final String DEFAULT_LOCALSTACK_ACCESS_KEY = "test";
    public static final String DEFAULT_LOCALSTACK_SECRET_KEY = "test";
    
    /**
     * 기본 파일명 (파일명이 없을 때 사용)
     */
    public static final String DEFAULT_FILENAME = "file";
    
    /**
     * 파일 키 구분자
     */
    public static final String FILE_KEY_SEPARATOR = "_";
}

