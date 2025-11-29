package com.mxxikr.couponadmin.application.event;

import com.mxxikr.couponadmin.application.CouponService;
import com.mxxikr.couponadmin.application.FileUploadService;
import com.mxxikr.couponadmin.application.port.out.StorageService;
import com.mxxikr.couponadmin.common.exception.BusinessException;
import com.mxxikr.couponadmin.common.exception.ErrorCode;
import com.mxxikr.couponadmin.domain.CouponIssuanceJob;
import com.mxxikr.couponadmin.domain.CouponIssuanceJobRepository;
import com.mxxikr.couponadmin.domain.CouponIssuanceFileMetadata;
import com.mxxikr.couponadmin.domain.CouponIssuanceFileMetadataRepository;
import com.mxxikr.couponadmin.domain.Coupon;
import com.mxxikr.couponadmin.common.logging.StructuredLogger;
import com.mxxikr.couponadmin.domain.event.CouponIssuanceRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 쿠폰 발급 요청 이벤트 핸들러
 * 비동기로 쿠폰 발급을 처리함
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssuanceEventHandler {

    private final CouponIssuanceJobRepository jobRepository;
    private final CouponIssuanceFileMetadataRepository metadataRepository;
    private final StorageService storageService;
    private final FileUploadService fileUploadService;
    private final CouponService couponService;

    /**
     * 쿠폰 발급 요청 이벤트를 처리함
     *
     * @param event 쿠폰 발급 요청 이벤트
     */
    @Async("couponIssuanceExecutor")
    @EventListener
    @Transactional
    public void handleCouponIssuanceRequest(CouponIssuanceRequestEvent event) {
        long startTime = System.currentTimeMillis();
        CouponIssuanceJob job = jobRepository.findById(event.jobId())
                .orElseThrow(() -> {
                    StructuredLogger.logError("JOB_NOT_FOUND", ErrorCode.JOB_NOT_FOUND.getCode(), 
                            "작업을 찾을 수 없습니다", null, Map.of("jobId", event.jobId()));
                    return new BusinessException(ErrorCode.JOB_NOT_FOUND);
                });

        Map<String, String> jobContext = new HashMap<>();
        jobContext.put("fileId", event.fileId());
        jobContext.put("fileName", event.fileName());
        jobContext.put("couponName", event.couponName());
        StructuredLogger.logJobStart(event.jobId(), "COUPON_ISSUANCE", jobContext);

        try {
            log.info("쿠폰 발급 작업 시작: jobId={}, fileId={}, fileName={}, couponName={}", 
                    job.getJobId(), event.fileId(), event.fileName(), event.couponName());
            
            // 작업 상태를 처리 중으로 변경
            job.startProcessing();
            jobRepository.save(job);

            // 파일 메타데이터 조회
            CouponIssuanceFileMetadata metadata = metadataRepository.findById(event.fileId())
                    .orElseThrow(() -> {
                        StructuredLogger.logError("FILE_NOT_FOUND", ErrorCode.FILE_NOT_FOUND.getCode(), 
                                "파일을 찾을 수 없습니다", null, Map.of("fileId", event.fileId(), "jobId", event.jobId()));
                        return new BusinessException(ErrorCode.FILE_NOT_FOUND);
                    });

            // 파일을 스트리밍 방식으로 다운로드하여 파싱 및 쿠폰 발급
            AtomicLong processedCount = new AtomicLong(0);
            
            try (InputStream inputStream = storageService.downloadAsStream(metadata.getStoredFilePath())) {
                List<Coupon> batch = new ArrayList<>();
                
                fileUploadService.processFileStream(
                        inputStream, 
                        event.fileName(), 
                        customerId -> {
                            processedCount.incrementAndGet();
                            couponService.addToBatch(
                                    customerId, 
                                    event.couponName(), 
                                    event.expiresAt(), 
                                    batch
                            );
                        }
                );

                // 미처리 배치 저장
                couponService.saveRemainingBatch(batch);
            }

            // 작업 완료 처리
            long duration = System.currentTimeMillis() - startTime;
            job.complete(processedCount.get());
            jobRepository.save(job);
            
            Map<String, String> result = new HashMap<>();
            result.put("totalProcessed", String.valueOf(processedCount.get()));
            StructuredLogger.logJobComplete(event.jobId(), "COUPON_ISSUANCE", duration, result);
            log.info("쿠폰 발급 작업 완료: jobId={}, processedCount={}, durationMs={}", 
                    job.getJobId(), processedCount.get(), duration);

        } catch (BusinessException e) {
            long duration = System.currentTimeMillis() - startTime;
            StructuredLogger.logJobFailure(event.jobId(), "COUPON_ISSUANCE", 
                    e.getErrorCode().getCode(), e.getMessage(), e);
            log.error("쿠폰 발급 작업 실패: jobId={}, errorCode={}, error={}, durationMs={}", 
                    job.getJobId(), e.getErrorCode().getCode(), e.getMessage(), duration, e);
            job.fail(e.getMessage());
            jobRepository.save(job);
            throw e;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            StructuredLogger.logJobFailure(event.jobId(), "COUPON_ISSUANCE", 
                    ErrorCode.FILE_PARSING_FAILED.getCode(), "처리 중 오류가 발생했습니다: " + e.getMessage(), e);
            log.error("쿠폰 발급 작업 중 예상치 못한 오류 발생: jobId={}, durationMs={}", 
                    job.getJobId(), duration, e);
            job.fail("처리 중 오류가 발생했습니다: " + e.getMessage());
            jobRepository.save(job);
            throw new BusinessException(ErrorCode.FILE_PARSING_FAILED, e);
        }
    }
}