package com.mxxikr.couponadmin.application;

import com.mxxikr.couponadmin.application.port.out.FileParserPort;
import com.mxxikr.couponadmin.common.FileType;
import com.mxxikr.couponadmin.common.exception.BusinessException;
import com.mxxikr.couponadmin.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 파일 파서 선택 전략
 * 파일 확장자에 따라 적절한 파서를 반환함
 */
@Component
public class FileParserStrategy {

    private final List<FileParserPort> parsers;

    public FileParserStrategy(List<FileParserPort> parsers) {
        this.parsers = parsers;
    }

    /**
     * 파일명에 따라 적절한 FileParserPort를 반환함
     * @param filename 파일명
     * @return FileParserPort 구현체
     */
    public FileParserPort getParser(String filename) {
        FileType fileType = FileType.fromFilename(filename);
        
        return parsers.stream()
                .filter(parser -> parser.supports(fileType))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_FILE_PARSER));
    }
}
