package com.mxxikr.couponadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 쿠폰 발급 작업 응답 DTO
 * 쿠폰 발급 요청 시 작업 ID를 반환
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CouponIssuanceJobResponseDTO {
    private String jobId;
}

