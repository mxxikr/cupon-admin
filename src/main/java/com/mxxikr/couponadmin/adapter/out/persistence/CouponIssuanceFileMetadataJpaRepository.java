package com.mxxikr.couponadmin.adapter.out.persistence;

import com.mxxikr.couponadmin.domain.CouponIssuanceFileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 쿠폰 발급 파일 메타데이터 JPA 리포지토리
 */
public interface CouponIssuanceFileMetadataJpaRepository extends JpaRepository<CouponIssuanceFileMetadata, String> {
}
