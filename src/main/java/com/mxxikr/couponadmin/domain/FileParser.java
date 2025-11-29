package com.mxxikr.couponadmin.domain;

import com.mxxikr.couponadmin.common.exception.FileParsingException;

import java.io.InputStream;
import java.util.function.Consumer;

/**
 * 파일 파싱을 위한 인터페이스 정의함
 * 스트리밍 방식으로 처리하여 OOM 방지
 */
public interface FileParser {
    /**
     * 입력 스트림으로부터 고객 ID를 스트리밍 방식으로 파싱함
     * 각 고객 ID를 발견할 때마다 consumer를 호출함
     * @param inputStream 파일의 입력 스트림
     * @param customerIdConsumer 각 고객 ID를 처리할 Consumer
     * @throws FileParsingException 파싱 중 오류 발생 시
     */
    void parseStream(InputStream inputStream, Consumer<Long> customerIdConsumer) throws FileParsingException;
}