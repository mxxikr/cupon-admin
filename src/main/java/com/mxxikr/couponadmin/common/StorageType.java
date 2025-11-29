package com.mxxikr.couponadmin.common;

/**
 * 스토리지 타입 열거형
 * 전략 패턴에서 사용할 스토리지 타입을 정의함
 */
public enum StorageType {
    LOCAL("local"),
    S3("s3");

    private final String value;

    StorageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * 문자열 값으로 StorageType을 찾음
     * @param value 문자열 값
     * @return StorageType 열거형, 없으면 LOCAL을 기본값으로 반환
     */
    public static StorageType fromString(String value) {
        if (value == null || value.isEmpty()) {
            return LOCAL;
        }
        
        for (StorageType type : StorageType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        
        return LOCAL;
    }
}

