package com.mxxikr.couponadmin.application.port.out;

import com.mxxikr.couponadmin.domain.CouponIssuanceJob;
import java.util.Optional;

/**
 * 쿠폰 발급 작업 영속성 포트
 */
public interface CouponIssuanceJobRepository {
    CouponIssuanceJob save(CouponIssuanceJob job);
    Optional<CouponIssuanceJob> findById(String jobId);
}
