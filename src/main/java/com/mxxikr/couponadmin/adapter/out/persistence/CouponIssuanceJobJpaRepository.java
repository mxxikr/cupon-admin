package com.mxxikr.couponadmin.adapter.out.persistence;

import com.mxxikr.couponadmin.domain.CouponIssuanceJob;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 쿠폰 발급 작업 상태 저장소
 */
public interface CouponIssuanceJobJpaRepository extends JpaRepository<CouponIssuanceJob, String> {
}

