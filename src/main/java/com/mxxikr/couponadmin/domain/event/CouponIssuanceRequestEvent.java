package com.mxxikr.couponadmin.domain.event;

import java.time.LocalDateTime;

/**
 * 쿠폰 발급 요청 이벤트
 * 파일 업로드 후 쿠폰 발급을 비동기로 처리하기 위한 이벤트
 */
public record CouponIssuanceRequestEvent(String jobId, String fileId, String fileName, String couponName, LocalDateTime expiresAt) {
}

