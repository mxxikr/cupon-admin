package com.mxxikr.couponadmin.application.port.out;

import com.mxxikr.couponadmin.domain.CouponIssuanceFileMetadata;
import java.util.Optional;

/**
 * 파일 메타데이터 영속성 포트
 */
public interface CouponIssuanceFileMetadataRepository {
    CouponIssuanceFileMetadata save(CouponIssuanceFileMetadata metadata);
    Optional<CouponIssuanceFileMetadata> findById(String fileId);
}
