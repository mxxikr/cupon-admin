package com.mxxikr.couponadmin.application;

import com.mxxikr.couponadmin.application.port.out.CouponIssuanceFileMetadataRepository;
import com.mxxikr.couponadmin.application.port.out.CouponIssuanceJobRepository;
import com.mxxikr.couponadmin.application.port.out.StorageService;
import com.mxxikr.couponadmin.common.exception.BusinessException;
import com.mxxikr.couponadmin.common.exception.ErrorCode;
import com.mxxikr.couponadmin.domain.CouponIssuanceFileMetadata;
import com.mxxikr.couponadmin.domain.CouponIssuanceJob;
import com.mxxikr.couponadmin.domain.event.CouponIssuanceRequestEvent;
import com.mxxikr.couponadmin.dto.CouponIssuanceJobStatusDTO;
import com.mxxikr.couponadmin.dto.FileDownloadResourceDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.time.LocalDateTime;

/**
 * 쿠폰 대량 발급 요청을 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponIssuanceService {

    private final CouponIssuanceFileMetadataRepository metadataRepository;
    private final CouponIssuanceJobRepository jobRepository;
    private final CouponService couponService;
    private final StorageService storageService;
    private final FileUploadService fileUploadService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public String uploadFileAndIssueCoupons(MultipartFile file, String couponName, LocalDateTime expiresAt) {
        log.info("쿠폰 발급 요청 수신: fileName={}, couponName={}, expiresAt={}", 
                file.getOriginalFilename(), couponName, expiresAt);
        
        String fileId = fileUploadService.uploadFile(file);
        return createJobAndPublishEvent(fileId, file.getOriginalFilename(), couponName, expiresAt);
    }

    @Transactional(readOnly = true)
    public FileDownloadResourceDTO downloadFileAsResource(String fileId) {
        log.debug("파일 다운로드 요청: fileId={}", fileId);

        CouponIssuanceFileMetadata metadata = findMetadataById(fileId);
        Resource resource = storageService.loadAsResource(metadata.getStoredFilePath());
        
        return new FileDownloadResourceDTO(metadata.getOriginalFileName(), resource);
    }
    
    private CouponIssuanceFileMetadata findMetadataById(String fileId) {
        return metadataRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));
    }

    @Transactional
    public String processS3UploadedFile(String fileKey, String originalFileName, String couponName, LocalDateTime expiresAt) {
        String fileId = fileUploadService.saveS3FileMetadata(fileKey, originalFileName);
        return createJobAndPublishEvent(fileId, originalFileName, couponName, expiresAt);
    }
    
    private String createJobAndPublishEvent(String fileId, String fileName, String couponName, LocalDateTime expiresAt) {
        CouponIssuanceJob job = CouponIssuanceJob.builder()
                .fileId(fileId)
                .fileName(fileName)
                .couponName(couponName)
                .expiresAt(expiresAt)
                .build();
        
        CouponIssuanceJob savedJob = jobRepository.save(job);
        
        CouponIssuanceRequestEvent event = new CouponIssuanceRequestEvent(
                savedJob.getJobId(),
                fileId,
                fileName,
                couponName,
                expiresAt
        );
        eventPublisher.publishEvent(event);
        
        return savedJob.getJobId();
    }

    public StorageService.PresignedUrlResult generateUploadPresignedUrl(String fileName, int expirationMinutes) {
        return storageService.generateUploadPresignedUrl(fileName, expirationMinutes);
    }

    @Transactional(readOnly = true)
    public URL generateDownloadPresignedUrl(String fileId, int expirationMinutes) {
        CouponIssuanceFileMetadata metadata = findMetadataById(fileId);
        return storageService.generateDownloadPresignedUrl(metadata.getStoredFilePath(), expirationMinutes);
    }

    @Transactional(readOnly = true)
    public CouponIssuanceJobStatusDTO getJobStatus(String jobId) {
        log.debug("작업 상태 조회: jobId={}", jobId);
        CouponIssuanceJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException(ErrorCode.JOB_NOT_FOUND));
        
        return new CouponIssuanceJobStatusDTO(
                job.getJobId(),
                job.getFileId(),
                job.getFileName(),
                job.getCouponName(),
                job.getStatus(),
                job.getTotalProcessed(),
                job.getErrorMessage(),
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }
}
