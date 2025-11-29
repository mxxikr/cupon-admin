package com.mxxikr.couponadmin.dto;

import com.mxxikr.couponadmin.domain.CouponIssuanceJob;
import java.time.LocalDateTime;

/**
 * 쿠폰 발급 작업 상태 DTO
 * 서비스 레이어에서 작업 상태 정보를 전달할 때 사용
 */
public record CouponIssuanceJobStatusDTO(
        String jobId,
        String fileId,
        String fileName,
        String couponName,
        CouponIssuanceJob.JobStatus status,
        Long totalProcessed,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

