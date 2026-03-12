package com.mxxikr.couponadmin.application;

import com.mxxikr.couponadmin.application.port.out.CouponIssuanceFileMetadataRepository;
import com.mxxikr.couponadmin.application.port.out.FileParserPort;
import com.mxxikr.couponadmin.application.port.out.StorageService;
import com.mxxikr.couponadmin.common.exception.BusinessException;
import com.mxxikr.couponadmin.common.exception.ErrorCode;
import com.mxxikr.couponadmin.domain.CouponIssuanceFileMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
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

    @Transactional
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_IS_EMPTY);
        }

        String storedPath = storageService.store(file);

        CouponIssuanceFileMetadata metadata = CouponIssuanceFileMetadata.builder()
                .originalFileName(file.getOriginalFilename())
                .storedFilePath(storedPath)
                .build();

        CouponIssuanceFileMetadata savedMetadata = metadataRepository.save(metadata);
        
        return savedMetadata.getFileId();
    }

    @Transactional
    public String saveS3FileMetadata(String fileKey, String originalFileName) {
        CouponIssuanceFileMetadata metadata = CouponIssuanceFileMetadata.builder()
                .originalFileName(originalFileName)
                .storedFilePath(fileKey)
                .build();

        CouponIssuanceFileMetadata savedMetadata = metadataRepository.save(metadata);
        
        return savedMetadata.getFileId();
    }

    public void processFileStream(InputStream inputStream, String fileName, java.util.function.Consumer<Long> customerIdConsumer) {
        FileParserPort fileParser = fileParserStrategy.getParser(fileName);
        AtomicBoolean hasValidCustomerId = new AtomicBoolean(false);
        AtomicLong processedCount = new AtomicLong(0);

        try {
            fileParser.parse(inputStream, customerId -> {
                hasValidCustomerId.set(true);
                processedCount.incrementAndGet();
                customerIdConsumer.accept(customerId);
            });

            if (!hasValidCustomerId.get()) {
                throw new BusinessException(ErrorCode.EMPTY_CUSTOMER_LIST);
            }
            
            log.debug("파일 스트림 처리 완료: fileName={}, processedCount={}", 
                    fileName, processedCount.get());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_PARSING_FAILED, e);
        }
    }
}
