package com.mxxikr.couponadmin.application;

import com.mxxikr.couponadmin.application.port.out.CouponRepository;
import com.mxxikr.couponadmin.domain.Coupon;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 개별 쿠폰 관련 비즈니스 로직을 처리
 */
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    @Value("${coupon.batch-size:1000}")
    private int batchSize;

    @Transactional
    public void issueCouponsInBulk(List<Long> customerIds, String couponName, LocalDateTime expiresAt) {
        List<Coupon> batch = new ArrayList<>();
        
        for (Long customerId : customerIds) {
            batch.add(Coupon.builder()
                    .customerId(customerId)
                    .couponName(couponName)
                    .expiresAt(expiresAt)
                    .build());
            
            if (batch.size() >= batchSize) {
                couponRepository.saveAll(batch);
                batch.clear();
            }
        }
        
        saveRemainingBatch(batch);
    }

    public boolean addToBatch(Long customerId, String couponName, LocalDateTime expiresAt, List<Coupon> batch) {
        batch.add(Coupon.builder()
                .customerId(customerId)
                .couponName(couponName)
                .expiresAt(expiresAt)
                .build());
        
        if (batch.size() >= batchSize) {
            couponRepository.saveAll(batch);
            batch.clear();
            return true;
        }
        return false;
    }
    
    public void saveRemainingBatch(List<Coupon> batch) {
        if (!batch.isEmpty()) {
            couponRepository.saveAll(batch);
            batch.clear();
        }
    }
}
