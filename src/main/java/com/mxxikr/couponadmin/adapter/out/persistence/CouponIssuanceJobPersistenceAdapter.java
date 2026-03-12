package com.mxxikr.couponadmin.adapter.out.persistence;

import com.mxxikr.couponadmin.application.port.out.CouponIssuanceJobRepository;
import com.mxxikr.couponadmin.domain.CouponIssuanceJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CouponIssuanceJobPersistenceAdapter implements CouponIssuanceJobRepository {

    private final CouponIssuanceJobJpaRepository jobJpaRepository;

    @Override
    public CouponIssuanceJob save(CouponIssuanceJob job) {
        return jobJpaRepository.save(job);
    }

    @Override
    public Optional<CouponIssuanceJob> findById(String jobId) {
        return jobJpaRepository.findById(jobId);
    }
}
