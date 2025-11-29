package com.mxxikr.couponadmin.application;

import com.mxxikr.couponadmin.application.port.out.StorageService;
import com.mxxikr.couponadmin.common.exception.BusinessException;
import com.mxxikr.couponadmin.common.exception.ErrorCode;
import com.mxxikr.couponadmin.common.logging.StructuredLogger;
import com.mxxikr.couponadmin.domain.*;
import com.mxxikr.couponadmin.domain.CouponIssuanceFileMetadata;
import com.mxxikr.couponadmin.domain.CouponIssuanceFileMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 파일 업로드 관련 비즈니스 로직을 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final CouponIssuanceFileMetadataRepository metadataRepository;
    private final StorageService storageService;
    private final FileParserStrategy fileParserStrategy;

    /**
     * 파일을 저장하고 메타데이터를 반환함
     * @param file 업로드된 MultipartFile
     * @return 저장된 파일의 고유 ID
     */
    @Transactional
    public String uploadFile(MultipartFile file) {
        long startTime = System.currentTimeMillis();
        
        // 파일 유효성 검증
        if (file == null || file.isEmpty()) {
            StructuredLogger.logError("FILE_VALIDATION", ErrorCode.FILE_IS_EMPTY.getCode(), 
                    "파일이 비어있거나 null입니다", null, null);
            throw new BusinessException(ErrorCode.FILE_IS_EMPTY);
        }

        log.debug("파일 업로드 시작: fileName={}, size={}", file.getOriginalFilename(), file.getSize());

        // 파일 저장 수행
        String storedPath = storageService.store(file);

        // 파일 메타데이터 저장
        CouponIssuanceFileMetadata metadata = CouponIssuanceFileMetadata.builder()
                .originalFileName(file.getOriginalFilename())
                .storedFilePath(storedPath)
                .build();

        CouponIssuanceFileMetadata savedMetadata = metadataRepository.save(metadata);
        
        long duration = System.currentTimeMillis() - startTime;
        Map<String, String> context = new HashMap<>();
        context.put("fileId", savedMetadata.getFileId());
        context.put("fileName", file.getOriginalFilename());
        context.put("fileSize", String.valueOf(file.getSize()));
        StructuredLogger.logBusinessEvent("FILE_UPLOADED", context, 
                String.format("파일 업로드 완료: fileId=%s, fileName=%s", savedMetadata.getFileId(), file.getOriginalFilename()));
        StructuredLogger.logPerformance("FILE_UPLOAD", duration, context);
        
        return savedMetadata.getFileId();
    }

    /**
     * S3에 업로드된 파일의 메타데이터를 저장함
     * @param fileKey S3에 저장된 파일의 키
     * @param originalFileName 원본 파일명
     * @return 저장된 파일의 고유 ID
     */
    @Transactional
    public String saveS3FileMetadata(String fileKey, String originalFileName) {
        CouponIssuanceFileMetadata metadata = CouponIssuanceFileMetadata.builder()
                .originalFileName(originalFileName)
                .storedFilePath(fileKey)
                .build();

        CouponIssuanceFileMetadata savedMetadata = metadataRepository.save(metadata);
        
        Map<String, String> context = new HashMap<>();
        context.put("fileId", savedMetadata.getFileId());
        context.put("fileKey", fileKey);
        context.put("fileName", originalFileName);
        StructuredLogger.logBusinessEvent("S3_FILE_METADATA_SAVED", context, 
                String.format("S3 파일 메타데이터 저장 완료: fileId=%s, fileKey=%s", savedMetadata.getFileId(), fileKey));
        
        return savedMetadata.getFileId();
    }

    /**
     * 파일에서 고객 ID를 스트리밍 방식으로 추출하여 처리함
     * @param inputStream 파일의 입력 스트림
     * @param fileName 파일명 (확장자 확인용)
     * @param customerIdConsumer 각 고객 ID를 처리할 Consumer
     */
    public void processFileStream(InputStream inputStream, String fileName, java.util.function.Consumer<Long> customerIdConsumer) {
        long startTime = System.currentTimeMillis();
        FileParser fileParser = fileParserStrategy.getParser(fileName);
        AtomicBoolean hasValidCustomerId = new AtomicBoolean(false);
        AtomicLong processedCount = new AtomicLong(0);

        log.debug("파일 스트림 처리 시작: fileName={}", fileName);

        try {
            fileParser.parseStream(inputStream, customerId -> {
                hasValidCustomerId.set(true);
                processedCount.incrementAndGet();
                customerIdConsumer.accept(customerId);
            });

            if (!hasValidCustomerId.get()) {
                StructuredLogger.logError("FILE_PARSING", ErrorCode.EMPTY_CUSTOMER_LIST.getCode(), 
                        "고객 목록이 비어있습니다", null, Map.of("fileName", fileName));
                throw new BusinessException(ErrorCode.EMPTY_CUSTOMER_LIST);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            Map<String, String> context = new HashMap<>();
            context.put("fileName", fileName);
            context.put("processedCount", String.valueOf(processedCount.get()));
            StructuredLogger.logPerformance("FILE_STREAM_PROCESSING", duration, context);
            log.debug("파일 스트림 처리 완료: fileName={}, processedCount={}, durationMs={}", 
                    fileName, processedCount.get(), duration);
        } catch (BusinessException e) {
            StructuredLogger.logError("FILE_PARSING", e.getErrorCode().getCode(), 
                    e.getMessage(), e, Map.of("fileName", fileName));
            throw e;
        } catch (Exception e) {
            StructuredLogger.logError("FILE_PARSING", ErrorCode.FILE_PARSING_FAILED.getCode(), 
                    "파일 파싱 중 오류 발생", e, Map.of("fileName", fileName));
            throw new BusinessException(ErrorCode.FILE_PARSING_FAILED, e);
        }
    }
}

