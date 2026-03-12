package com.mxxikr.couponadmin.application.port.out;

import com.mxxikr.couponadmin.domain.Coupon;
import java.util.List;

/**
 * 쿠폰 영속성 포트
 * 도메인 계층에서 쿠폰 데이터를 저장하고 관리하기 위한 명세
 */
public interface CouponRepository {
    void save(Coupon coupon);
    void saveAll(List<Coupon> coupons);
}
