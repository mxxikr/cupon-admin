package com.mxxikr.couponadmin.adapter.out.persistence;

import com.mxxikr.couponadmin.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 쿠폰 JPA 리포지토리
 */
public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {
}
