package com.mxxikr.couponadmin.domain;

import com.mxxikr.couponadmin.common.constants.FileConstants;
import com.mxxikr.couponadmin.common.exception.ErrorCode;
import com.mxxikr.couponadmin.common.exception.FileParsingException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Excel 파일 파서 구현체
 * 스트리밍 방식으로 처리하여 OOM 방지
 */
@Component
public class ExcelFileParser implements FileParser {

    @Override
    public void parseStream(InputStream inputStream, Consumer<Long> customerIdConsumer) throws FileParsingException {
        // try-with-resources로 Workbook을 자동으로 닫아 메모리 해제
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(FileConstants.EXCEL_HEADER_ROW_INDEX);

            validateHeader(sheet.getRow(FileConstants.EXCEL_HEADER_ROW_INDEX));

            extractCustomerIds(sheet, customerIdConsumer);
        } catch (FileParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new FileParsingException(ErrorCode.FILE_PARSING_FAILED, e);
        }
    }

    /**
     * 헤더의 유효성을 검증함
     * 첫 번째 열의 헤더는 customer_id여야 함
     * @param headerRow 헤더 행
     */
    private void validateHeader(Row headerRow) {
        if (headerRow == null 
                || headerRow.getCell(FileConstants.EXCEL_FIRST_COLUMN_INDEX) == null 
                || !Objects.equals(
                    headerRow.getCell(FileConstants.EXCEL_FIRST_COLUMN_INDEX).getStringCellValue(), 
                    FileConstants.CUSTOMER_ID_HEADER)) {
            throw new FileParsingException(ErrorCode.INVALID_FILE_HEADER);
        }
    }

    /**
     * Excel 시트에서 고객 ID를 스트리밍 방식으로 추출함
     * @param sheet Excel 시트
     * @param customerIdConsumer 각 고객 ID를 처리할 Consumer
     */
    private void extractCustomerIds(Sheet sheet, Consumer<Long> customerIdConsumer) {
        for (int i = FileConstants.EXCEL_FIRST_DATA_ROW_INDEX; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                Cell cell = row.getCell(FileConstants.EXCEL_FIRST_COLUMN_INDEX);
                if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                    customerIdConsumer.accept((long) cell.getNumericCellValue());
                } else if (cell != null && cell.getCellType() == CellType.STRING) {
                    try {
                        customerIdConsumer.accept(Long.parseLong(cell.getStringCellValue()));
                    } catch (NumberFormatException e) {
                        // 숫자 형식으로 변환할 수 없는 문자열은 무시함
                    }
                }
            }
        }
    }
}