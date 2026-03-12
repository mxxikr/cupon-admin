package com.mxxikr.couponadmin.application.port.out;

import com.mxxikr.couponadmin.common.FileType;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * 파일 파싱 포트
 */
public interface FileParserPort {
    void parse(InputStream inputStream, Consumer<Long> customerIdConsumer);
    boolean supports(FileType fileType);
}
