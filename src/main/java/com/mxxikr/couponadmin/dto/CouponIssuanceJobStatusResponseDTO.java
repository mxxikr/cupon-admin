package com.mxxikr.couponadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 쿠폰 발급 작업 상태 응답 DTO
 * 작업 상태 조회 시 사용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CouponIssuanceJobStatusResponseDTO {
    private String jobId;
    private String fileId;
    private String fileName;
    private String couponName;
    private String status;
    private Long totalProcessed;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

