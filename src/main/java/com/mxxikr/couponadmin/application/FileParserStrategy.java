package com.mxxikr.couponadmin.application;

import com.mxxikr.couponadmin.common.FileType;
import com.mxxikr.couponadmin.common.exception.BusinessException;
import com.mxxikr.couponadmin.common.exception.ErrorCode;
import com.mxxikr.couponadmin.domain.CsvFileParser;
import com.mxxikr.couponadmin.domain.ExcelFileParser;
import com.mxxikr.couponadmin.domain.FileParser;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 파일 파서 선택 전략
 * 파일 확장자에 따라 적절한 파서를 반환함
 * Enum 기반 전략 패턴으로 개선하여 확장성과 유지보수성 향상
 */
@Component
public class FileParserStrategy {

    private final Map<FileType, FileParser> parserMap;

    public FileParserStrategy(CsvFileParser csvFileParser, ExcelFileParser excelFileParser) {
        // 전략 맵 초기화: FileType과 FileParser 구현체를 매핑
        parserMap = new HashMap<>();
        parserMap.put(FileType.CSV, csvFileParser);
        parserMap.put(FileType.XLS, excelFileParser);
        parserMap.put(FileType.XLSX, excelFileParser);
    }

    /**
     * 파일명에 따라 적절한 FileParser를 반환함
     * @param filename 파일명
     * @return FileParser 구현체
     */
    public FileParser getParser(String filename) {
        FileType fileType = FileType.fromFilename(filename);
        FileParser parser = parserMap.get(fileType);
        
        if (parser == null) {
            throw new BusinessException(ErrorCode.INVALID_FILE_PARSER);
        }
        
        return parser;
    }
}

