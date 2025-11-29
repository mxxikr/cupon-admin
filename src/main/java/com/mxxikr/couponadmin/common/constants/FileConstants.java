package com.mxxikr.couponadmin.common.constants;

/**
 * 파일 관련 상수 정의
 */
public final class FileConstants {
    
    private FileConstants() {
        throw new AssertionError("상수 클래스는 인스턴스화할 수 없습니다");
    }

    /**
     * CSV/Excel 파일의 헤더 컬럼명
     */
    public static final String CUSTOMER_ID_HEADER = "customer_id";
    
    /**
     * Excel 시트의 첫 번째 행 인덱스 (헤더)
     */
    public static final int EXCEL_HEADER_ROW_INDEX = 0;
    
    /**
     * Excel 시트의 첫 번째 데이터 행 인덱스
     */
    public static final int EXCEL_FIRST_DATA_ROW_INDEX = 1;
    
    /**
     * Excel 시트의 첫 번째 컬럼 인덱스
     */
    public static final int EXCEL_FIRST_COLUMN_INDEX = 0;
    
    /**
     * CSV 파일의 첫 번째 컬럼 인덱스
     */
    public static final int CSV_FIRST_COLUMN_INDEX = 0;
}

