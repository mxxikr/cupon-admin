package com.mxxikr.couponadmin.application;

import com.mxxikr.couponadmin.application.port.out.StorageService;
import com.mxxikr.couponadmin.common.exception.BusinessException;
import com.mxxikr.couponadmin.common.exception.ErrorCode;
import com.mxxikr.couponadmin.dto.FileDownloadResourceDTO;
import com.mxxikr.couponadmin.dto.CouponIssuanceJobStatusDTO;
import com.mxxikr.couponadmin.domain.Coupon;
import com.mxxikr.couponadmin.domain.CouponIssuanceFileMetadata;
import com.mxxikr.couponadmin.domain.CouponIssuanceFileMetadataRepository;
import com.mxxikr.couponadmin.domain.CouponIssuanceJob;
import com.mxxikr.couponadmin.domain.CouponIssuanceJobRepository;
import com.mxxikr.couponadmin.common.logging.StructuredLogger;
import com.mxxikr.couponadmin.domain.event.CouponIssuanceRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 쿠폰 대량 발급 요청을 처리
 * 파일 업로드와 쿠폰 발급 로직이 분리되어 있음
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

    /**
     * 파일을 업로드하고 고객 목록을 추출하여 각 고객에게 쿠폰을 대량 발급함

     * @param file 업로드된 MultipartFile
     * @param couponName 발급할 쿠폰 이름
     * @param expiresAt 쿠폰 만료일시
     * @return 작업 ID (jobId)
     */
    @Transactional
    public String uploadFileAndIssueCoupons(MultipartFile file, String couponName, LocalDateTime expiresAt) {
        long startTime = System.currentTimeMillis();
        
        log.info("쿠폰 발급 요청 수신: fileName={}, couponName={}, expiresAt={}", 
                file.getOriginalFilename(), couponName, expiresAt);
        
        // 파일 업로드 수행
        String fileId = fileUploadService.uploadFile(file);
        
        // 쿠폰 발급 작업 생성 및 이벤트 발행
        String jobId = createJobAndPublishEvent(fileId, file.getOriginalFilename(), couponName, expiresAt);
        
        long duration = System.currentTimeMillis() - startTime;
        Map<String, String> context = new HashMap<>();
        context.put("jobId", jobId);
        context.put("fileId", fileId);
        context.put("couponName", couponName);
        StructuredLogger.logBusinessEvent("COUPON_ISSUANCE_REQUESTED", context, 
                String.format("쿠폰 발급 요청 완료: jobId=%s, fileId=%s", jobId, fileId));
        StructuredLogger.logPerformance("COUPON_ISSUANCE_REQUEST", duration, context);
        
        return jobId;
    }
    
    /**
     * 파일을 업로드하고 고객 목록을 추출하여 각 고객에게 쿠폰을 대량 발급함 (동기, 기존 방식)
     * 대용량 파일 처리 시 응답 시간이 길어질 수 있으므로 비동기 방식을 권장
     * @param file 업로드된 MultipartFile
     * @param couponName 발급할 쿠폰 이름
     * @param expiresAt 쿠폰 만료일시
     * @return 저장된 파일의 고유 ID
     * @deprecated 비동기 처리 방식을 사용하는 것을 권장합니다. uploadFileAndIssueCoupons를 사용하세요.
     */
    @Deprecated
    @Transactional
    public String uploadFileAndIssueCouponsSync(MultipartFile file, String couponName, LocalDateTime expiresAt) {
        // 파일 업로드 수행
        String fileId = fileUploadService.uploadFile(file);
        
        // 저장된 파일을 다시 읽어서 파싱 및 쿠폰 발급 수행
        CouponIssuanceFileMetadata metadata = findMetadataById(fileId);
        
        try (InputStream inputStream = storageService.downloadAsStream(metadata.getStoredFilePath())) {
            issueCouponsFromFile(inputStream, file.getOriginalFilename(), couponName, expiresAt);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_PARSING_FAILED, e);
        }
        
        return fileId;
    }

    /**
     * 파일 ID를 사용하여 저장된 파일을 Resource로 반환함 (스트리밍 방식)
     * @param fileId 파일 고유 ID
     * @return 파일 메타데이터와 파일 Resource를 담은 DTO
     */
    @Transactional(readOnly = true)
    public FileDownloadResourceDTO downloadFileAsResource(String fileId) {
        log.debug("파일 다운로드 요청: fileId={}", fileId);
        CouponIssuanceFileMetadata metadata = findMetadataById(fileId);
        Resource resource = storageService.loadAsResource(metadata.getStoredFilePath());
        
        Map<String, String> context = new HashMap<>();
        context.put("fileId", fileId);
        context.put("fileName", metadata.getOriginalFileName());
        StructuredLogger.logBusinessEvent("FILE_DOWNLOAD", context, 
                String.format("파일 다운로드: fileId=%s, fileName=%s", fileId, metadata.getOriginalFileName()));
        
        return new FileDownloadResourceDTO(metadata.getOriginalFileName(), resource);
    }
    
    /**
     * 파일 ID로 메타데이터를 조회함 (코드 중복 제거)
     * @param fileId 파일 고유 ID
     * @return 파일 메타데이터
     */
    private CouponIssuanceFileMetadata findMetadataById(String fileId) {
        return metadataRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));
    }

    /**
     * S3에 업로드된 파일을 파싱하여 쿠폰을 대량 발급함 (비동기)
     * 파일 업로드와 쿠폰 발급 로직이 분리되어 있으며, Event-Driven 패턴으로 비동기 처리
     * @param fileKey S3에 저장된 파일의 키
     * @param originalFileName 원본 파일명
     * @param couponName 발급할 쿠폰 이름
     * @param expiresAt 쿠폰 만료일시
     * @return 작업 ID (jobId)
     */
    @Transactional
    public String processS3UploadedFile(String fileKey, String originalFileName, String couponName, LocalDateTime expiresAt) {
        // 파일 메타데이터 저장
        String fileId = fileUploadService.saveS3FileMetadata(fileKey, originalFileName);
        
        // 쿠폰 발급 작업 생성 및 이벤트 발행
        return createJobAndPublishEvent(fileId, originalFileName, couponName, expiresAt);
    }
    
    /**
     * 쿠폰 발급 작업을 생성하고 이벤트를 발행함 (코드 중복 제거)
     * @param fileId 파일 ID
     * @param fileName 파일명
     * @param couponName 쿠폰 이름
     * @param expiresAt 쿠폰 만료일시
     * @return 작업 ID
     */
    private String createJobAndPublishEvent(String fileId, String fileName, String couponName, LocalDateTime expiresAt) {
        CouponIssuanceJob job = CouponIssuanceJob.builder()
                .fileId(fileId)
                .fileName(fileName)
                .couponName(couponName)
                .expiresAt(expiresAt)
                .build();
        
        CouponIssuanceJob savedJob = jobRepository.save(job);
        
        log.debug("쿠폰 발급 작업 생성 완료: jobId={}, fileId={}, status={}", 
                savedJob.getJobId(), fileId, savedJob.getStatus());
        
        CouponIssuanceRequestEvent event = new CouponIssuanceRequestEvent(
                savedJob.getJobId(),
                fileId,
                fileName,
                couponName,
                expiresAt
        );
        eventPublisher.publishEvent(event);
        
        log.debug("쿠폰 발급 이벤트 발행 완료: jobId={}", savedJob.getJobId());
        
        return savedJob.getJobId();
    }
    
    /**
     * 파일에서 고객 ID를 스트리밍 방식으로 추출하여 쿠폰을 발급함
     * @param inputStream 파일의 입력 스트림
     * @param fileName 파일명
     * @param couponName 쿠폰 이름
     * @param expiresAt 쿠폰 만료일시
     */
    private void issueCouponsFromFile(InputStream inputStream, String fileName, String couponName, LocalDateTime expiresAt) {
        List<Coupon> batch = new ArrayList<>();
        AtomicBoolean hasValidCustomerId = new AtomicBoolean(false);
        
        // 파일 파서가 각 고객 ID를 발견할 때마다 배치에 추가
        fileUploadService.processFileStream(inputStream, fileName, customerId -> {
            hasValidCustomerId.set(true);
            couponService.addToBatch(customerId, couponName, expiresAt, batch);
        });
        
        if (!hasValidCustomerId.get()) {
            throw new BusinessException(ErrorCode.EMPTY_CUSTOMER_LIST);
        }
        
        // 미처리 배치 저장
        couponService.saveRemainingBatch(batch);
    }

    /**
     * 파일 업로드를 위한 Presigned URL을 생성함
     * @param fileName 업로드할 파일명
     * @param expirationMinutes URL 만료 시간 (분)
     * @return Presigned URL과 파일 키를 포함한 결과
     */
    public StorageService.PresignedUrlResult generateUploadPresignedUrl(String fileName, int expirationMinutes) {
        return storageService.generateUploadPresignedUrl(fileName, expirationMinutes);
    }

    /**
     * 파일 다운로드를 위한 Presigned URL을 생성함
     * @param fileId 파일 고유 ID
     * @param expirationMinutes URL 만료 시간 (분)
     * @return Presigned URL
     */
    @Transactional(readOnly = true)
    public URL generateDownloadPresignedUrl(String fileId, int expirationMinutes) {
        CouponIssuanceFileMetadata metadata = findMetadataById(fileId);
        return storageService.generateDownloadPresignedUrl(metadata.getStoredFilePath(), expirationMinutes);
    }

    /**
     * 쿠폰 발급 작업 상태를 조회함
     * @param jobId 작업 ID
     * @return 쿠폰 발급 작업 상태 정보
     */
    @Transactional(readOnly = true)
    public CouponIssuanceJobStatusDTO getJobStatus(String jobId) {
        log.debug("작업 상태 조회: jobId={}", jobId);
        CouponIssuanceJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> {
                    StructuredLogger.logError("JOB_NOT_FOUND", ErrorCode.JOB_NOT_FOUND.getCode(), 
                            "작업을 찾을 수 없습니다", null, Map.of("jobId", jobId));
                    return new BusinessException(ErrorCode.JOB_NOT_FOUND);
                });
        
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