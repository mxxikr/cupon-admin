package com.mxxikr.couponadmin.adapter.in.event;

import com.mxxikr.couponadmin.application.CouponIssuanceService;
import com.mxxikr.couponadmin.application.CouponService;
import com.mxxikr.couponadmin.application.FileUploadService;
import com.mxxikr.couponadmin.application.port.out.CouponIssuanceFileMetadataRepository;
import com.mxxikr.couponadmin.application.port.out.StorageService;
import com.mxxikr.couponadmin.common.exception.BusinessException;
import com.mxxikr.couponadmin.common.exception.ErrorCode;
import com.mxxikr.couponadmin.domain.CouponIssuanceFileMetadata;
import com.mxxikr.couponadmin.domain.Coupon;
import com.mxxikr.couponadmin.domain.event.CouponIssuanceRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 쿠폰 발급 요청 이벤트 핸들러
 * 비동기로 쿠폰 발급을 처리함
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssuanceEventHandler {

    private final CouponIssuanceService jobService;
    private final CouponIssuanceFileMetadataRepository metadataRepository;
    private final StorageService storageService;
    private final FileUploadService fileUploadService;
    private final CouponService couponService;

    /**
     * 쿠폰 발급 요청 이벤트를 처리함
     */
    @Async("couponIssuanceExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional
    public void handleCouponIssuanceRequest(CouponIssuanceRequestEvent event) {
        try {
            log.info("쿠폰 발급 작업 시작: jobId={}, fileId={}, fileName={}, couponName={}", 
                    event.jobId(), event.fileId(), event.fileName(), event.couponName());
            
            // 작업 상태를 처리 중으로 변경 (별도 트랜잭션)
            jobService.startJob(event.jobId());

            // 파일 메타데이터 조회
            CouponIssuanceFileMetadata metadata = metadataRepository.findById(event.fileId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));

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

            // 작업 완료 처리 (별도 트랜잭션)
            jobService.completeJob(event.jobId(), processedCount.get());
            
            log.info("쿠폰 발급 작업 완료: jobId={}, processedCount={}", 
                    event.jobId(), processedCount.get());

        } catch (BusinessException e) {
            log.error("쿠폰 발급 작업 실패: jobId={}, errorCode={}, error={}", 
                    event.jobId(), e.getErrorCode().getCode(), e.getMessage(), e);
            jobService.failJob(event.jobId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("쿠폰 발급 작업 중 예상치 못한 오류 발생: jobId={}", 
                    event.jobId(), e);
            jobService.failJob(event.jobId(), "처리 중 오류가 발생했습니다: " + e.getMessage());
            throw new BusinessException(ErrorCode.FILE_PARSING_FAILED, e);
        }
    }
}