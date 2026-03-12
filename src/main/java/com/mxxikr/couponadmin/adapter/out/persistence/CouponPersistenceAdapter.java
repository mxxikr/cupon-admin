package com.mxxikr.couponadmin.adapter.out.persistence;

import com.mxxikr.couponadmin.application.port.out.CouponRepository;
import com.mxxikr.couponadmin.domain.Coupon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CouponPersistenceAdapter implements CouponRepository {

    private final CouponJpaRepository couponJpaRepository;

    @Override
    public void save(Coupon coupon) {
        couponJpaRepository.save(coupon);
    }

    @Override
    public void saveAll(List<Coupon> coupons) {
        couponJpaRepository.saveAll(coupons);
    }
}
