package com.mxxikr.couponadmin.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 쿠폰 발급 작업 상태를 관리하는 엔티티
 * 비동기 처리 상태를 추적하기 위해 사용
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "coupon_issuance_jobs")
public class CouponIssuanceJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "job_id", updatable = false, nullable = false)
    private String jobId;

    @Column(nullable = false)
    private String fileId;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String couponName;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    private Long totalProcessed; // 처리된 쿠폰 수

    private String errorMessage; // 오류 메시지

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public CouponIssuanceJob(String fileId, String fileName, String couponName, LocalDateTime expiresAt) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.couponName = couponName;
        this.expiresAt = expiresAt;
        this.status = JobStatus.PENDING;
        this.totalProcessed = 0L;
    }

    /**
     * 작업 상태를 처리 중으로 변경
     */
    public void startProcessing() {
        this.status = JobStatus.PROCESSING;
    }

    /**
     * 작업을 완료 상태로 변경
     * @param totalProcessed 처리된 쿠폰 수
     */
    public void complete(Long totalProcessed) {
        this.status = JobStatus.COMPLETED;
        this.totalProcessed = totalProcessed;
    }

    /**
     * 작업을 실패 상태로 변경
     * @param errorMessage 오류 메시지
     */
    public void fail(String errorMessage) {
        this.status = JobStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    /**
     * 쿠폰 발급 작업 상태
     */
    public enum JobStatus {
        PENDING,      // 대기 중
        PROCESSING,   // 처리 중
        COMPLETED,    // 완료
        FAILED        // 실패
    }
}

