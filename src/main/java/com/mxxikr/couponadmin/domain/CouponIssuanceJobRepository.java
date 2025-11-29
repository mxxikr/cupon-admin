package com.mxxikr.couponadmin.domain;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 쿠폰 발급 작업 상태 저장소
 */
public interface CouponIssuanceJobRepository extends JpaRepository<CouponIssuanceJob, String> {
}

