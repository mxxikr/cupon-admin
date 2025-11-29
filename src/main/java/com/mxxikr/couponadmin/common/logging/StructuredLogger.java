package com.mxxikr.couponadmin.common.logging;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Map;

/**
 * 구조화된 로깅을 위한 유틸리티 클래스
 * MDC를 활용하여 컨텍스트 정보를 포함한 로깅 제공
 */
@Slf4j
public class StructuredLogger {

    /**
     * 비즈니스 이벤트 로깅
     * @param eventType 이벤트 타입 (예: "FILE_UPLOADED", "COUPON_ISSUED")
     * @param context 컨텍스트 정보 (key-value 쌍)
     * @param message 로그 메시지
     */
    public static void logBusinessEvent(String eventType, Map<String, String> context, String message) {
        try {
            if (context != null) {
                context.forEach(MDC::put);
            }
            MDC.put("eventType", eventType);
            log.info("[BUSINESS_EVENT] {}", message);
        } finally {
            MDC.clear();
        }
    }

    /**
     * 성능 메트릭 로깅
     * @param operation 작업명
     * @param durationMs 소요 시간 (밀리초)
     * @param context 추가 컨텍스트 정보
     */
    public static void logPerformance(String operation, long durationMs, Map<String, String> context) {
        try {
            if (context != null) {
                context.forEach(MDC::put);
            }
            MDC.put("operation", operation);
            MDC.put("durationMs", String.valueOf(durationMs));
            log.info("[PERFORMANCE] operation={}, durationMs={}", operation, durationMs);
        } finally {
            MDC.clear();
        }
    }

    /**
     * 에러 로깅 (구조화된 형식)
     * @param errorType 에러 타입
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     * @param throwable 예외 객체
     * @param context 추가 컨텍스트 정보
     */
    public static void logError(String errorType, String errorCode, String message, Throwable throwable, Map<String, String> context) {
        try {
            if (context != null) {
                context.forEach(MDC::put);
            }
            MDC.put("errorType", errorType);
            MDC.put("errorCode", errorCode);
            log.error("[ERROR] errorType={}, errorCode={}, message={}", errorType, errorCode, message, throwable);
        } finally {
            MDC.clear();
        }
    }

    /**
     * 작업 시작 로깅
     * @param jobId 작업 ID
     * @param operation 작업명
     * @param context 추가 컨텍스트 정보
     */
    public static void logJobStart(String jobId, String operation, Map<String, String> context) {
        try {
            if (context != null) {
                context.forEach(MDC::put);
            }
            MDC.put("jobId", jobId);
            MDC.put("operation", operation);
            log.info("[JOB_START] jobId={}, operation={}", jobId, operation);
        } finally {
            MDC.clear();
        }
    }

    /**
     * 작업 완료 로깅
     * @param jobId 작업 ID
     * @param operation 작업명
     * @param durationMs 소요 시간 (밀리초)
     * @param result 결과 정보
     */
    public static void logJobComplete(String jobId, String operation, long durationMs, Map<String, String> result) {
        try {
            if (result != null) {
                result.forEach(MDC::put);
            }
            MDC.put("jobId", jobId);
            MDC.put("operation", operation);
            MDC.put("durationMs", String.valueOf(durationMs));
            log.info("[JOB_COMPLETE] jobId={}, operation={}, durationMs={}", jobId, operation, durationMs);
        } finally {
            MDC.clear();
        }
    }

    /**
     * 작업 실패 로깅
     * @param jobId 작업 ID
     * @param operation 작업명
     * @param errorCode 에러 코드
     * @param errorMessage 에러 메시지
     * @param throwable 예외 객체
     */
    public static void logJobFailure(String jobId, String operation, String errorCode, String errorMessage, Throwable throwable) {
        try {
            MDC.put("jobId", jobId);
            MDC.put("operation", operation);
            MDC.put("errorCode", errorCode);
            log.error("[JOB_FAILURE] jobId={}, operation={}, errorCode={}, errorMessage={}", 
                    jobId, operation, errorCode, errorMessage, throwable);
        } finally {
            MDC.clear();
        }
    }
}

