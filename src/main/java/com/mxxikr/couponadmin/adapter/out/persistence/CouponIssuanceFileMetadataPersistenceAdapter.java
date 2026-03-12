package com.mxxikr.couponadmin.adapter.out.persistence;

import com.mxxikr.couponadmin.application.port.out.CouponIssuanceFileMetadataRepository;
import com.mxxikr.couponadmin.domain.CouponIssuanceFileMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CouponIssuanceFileMetadataPersistenceAdapter implements CouponIssuanceFileMetadataRepository {

    private final CouponIssuanceFileMetadataJpaRepository metadataJpaRepository;

    @Override
    public CouponIssuanceFileMetadata save(CouponIssuanceFileMetadata metadata) {
        return metadataJpaRepository.save(metadata);
    }

    @Override
    public Optional<CouponIssuanceFileMetadata> findById(String fileId) {
        return metadataJpaRepository.findById(fileId);
    }
}
