package com.mxxikr.couponadmin.application;

import com.mxxikr.couponadmin.domain.Coupon;
import com.mxxikr.couponadmin.domain.CouponRepository;
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

    /**
     * 여러 고객에게 쿠폰을 대량으로 발급함 (배치 처리)
     * @param customerIds 고객 ID 목록
     * @param couponName 쿠폰 이름
     * @param expiresAt 쿠폰 만료일시
     */
    @Transactional
    public void issueCouponsInBulk(List<Long> customerIds, String couponName, LocalDateTime expiresAt) {
        List<Coupon> batch = new ArrayList<>();
        
        for (Long customerId : customerIds) {
            batch.add(Coupon.builder()
                    .customerId(customerId)
                    .couponName(couponName)
                    .expiresAt(expiresAt)
                    .build());
            
            // 배치 크기 도달 시 저장
            if (batch.size() >= batchSize) {
                couponRepository.saveAll(batch);
                batch.clear();
            }
        }
        
        // 미처리 배치 저장
        saveRemainingBatch(batch);
    }

    /**
     * 스트리밍 방식으로 고객 ID를 받아 쿠폰을 발급함
     * 파일 파서가 각 고객 ID를 발견할 때마다 이 메서드가 호출됨
     * @param customerId 고객 ID
     * @param couponName 쿠폰 이름
     * @param expiresAt 쿠폰 만료일시
     * @param batch 현재 배치 리스트
     * @return 배치가 가득 찼는지 여부
     */
    public boolean addToBatch(Long customerId, String couponName, LocalDateTime expiresAt, List<Coupon> batch) {
        batch.add(Coupon.builder()
                .customerId(customerId)
                .couponName(couponName)
                .expiresAt(expiresAt)
                .build());
        
        // 배치 크기 도달 시 저장
        if (batch.size() >= batchSize) {
            couponRepository.saveAll(batch);
            batch.clear();
            return true;
        }
        return false;
    }
    
    /**
     * 미처리 배치 저장
     * @param batch 저장할 배치 리스트
     */
    public void saveRemainingBatch(List<Coupon> batch) {
        if (!batch.isEmpty()) {
            couponRepository.saveAll(batch);
            batch.clear();
        }
    }
}
