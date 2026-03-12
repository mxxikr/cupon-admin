package com.mxxikr.couponadmin.adapter.out.infrastructure;

import com.mxxikr.couponadmin.application.port.out.FileParserPort;
import com.mxxikr.couponadmin.common.FileType;
import com.mxxikr.couponadmin.common.constants.FileConstants;
import com.mxxikr.couponadmin.common.exception.ErrorCode;
import com.mxxikr.couponadmin.common.exception.FileParsingException;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * CSV 파일 파서 구현체
 * 스트리밍 방식으로 처리하여 OOM 방지
 */
@Component
public class CsvFileParser implements FileParserPort {

    @Override
    public void parse(InputStream inputStream, Consumer<Long> customerIdConsumer) throws FileParsingException {
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            String[] header = reader.readNext();
            validateHeader(header);

            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length > FileConstants.CSV_FIRST_COLUMN_INDEX 
                        && !line[FileConstants.CSV_FIRST_COLUMN_INDEX].trim().isEmpty()) {
                    try {
                        Long customerId = Long.parseLong(line[FileConstants.CSV_FIRST_COLUMN_INDEX].trim());
                        customerIdConsumer.accept(customerId);
                    } catch (NumberFormatException e) {
                        // 숫자 형식으로 변환할 수 없는 값은 무시함
                    }
                }
            }
        } catch (FileParsingException e) {
            throw e;
        } catch (IOException | CsvValidationException e) {
            throw new FileParsingException(ErrorCode.FILE_PARSING_FAILED, e);
        }
    }

    @Override
    public boolean supports(FileType fileType) {
        return fileType == FileType.CSV;
    }

    /**
     * 헤더의 유효성을 검증함. 첫 번째 열의 헤더는 'customer_id'여야 함
     * @param header 헤더 배열
     */
    private void validateHeader(String[] header) {
        if (header == null 
                || header.length == 0 
                || !Objects.equals(header[FileConstants.CSV_FIRST_COLUMN_INDEX], FileConstants.CUSTOMER_ID_HEADER)) {
            throw new FileParsingException(ErrorCode.INVALID_FILE_HEADER);
        }
    }
}
