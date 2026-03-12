package com.mxxikr.couponadmin.application.port.out;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;

/**
 * 파일 저장을 위한 Outgoing Port
 */
public interface StorageService {

    /**
     * 파일을 저장하고 저장된 경로/ID를 반환함
     */
    String store(MultipartFile file);


    /**
     * 저장된 경로의 파일을 Resource로 불러옴 (스트리밍 방식)
     */
    Resource loadAsResource(String storedPath);

    /**
     * 파일 업로드를 위한 Presigned URL을 생성함
     */
    PresignedUrlResult generateUploadPresignedUrl(String fileName, int expirationMinutes);

    /**
     * 파일 다운로드를 위한 Presigned URL을 생성함
     */
    URL generateDownloadPresignedUrl(String fileKey, int expirationMinutes);

    /**
     * S3에서 파일을 스트리밍 방식으로 다운로드하여 InputStream으로 반환함
     */
    InputStream downloadAsStream(String fileKey);

    /**
     * Presigned URL 생성 결과를 담는 레코드
     */
    record PresignedUrlResult(URL presignedUrl, String fileKey) {
    }
}
