package com.mxxikr.couponadmin.common;

import com.mxxikr.couponadmin.common.exception.BusinessException;
import com.mxxikr.couponadmin.common.exception.ErrorCode;

/**
 * 파일 타입 열거형
 * 전략 패턴에서 사용할 파일 타입을 정의함
 */
public enum FileType {
    CSV(".csv"),
    XLS(".xls"),
    XLSX(".xlsx");

    private final String extension;

    FileType(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    /**
     * 파일명으로 FileType을 찾음
     * @param filename 파일명
     * @return FileType 열거형
     * @throws BusinessException 지원하지 않는 파일 형식일 경우
     */
    public static FileType fromFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_FILE_TYPE);
        }

        String lowerFilename = filename.toLowerCase();
        
        for (FileType type : FileType.values()) {
            if (lowerFilename.endsWith(type.extension)) {
                return type;
            }
        }
        
        throw new BusinessException(ErrorCode.UNSUPPORTED_FILE_TYPE);
    }

    /**
     * Excel 파일인지 확인
     * @return Excel 파일이면 true
     */
    public boolean isExcel() {
        return this == XLS || this == XLSX;
    }

    /**
     * CSV 파일인지 확인
     * @return CSV 파일이면 true
     */
    public boolean isCsv() {
        return this == CSV;
    }
}

