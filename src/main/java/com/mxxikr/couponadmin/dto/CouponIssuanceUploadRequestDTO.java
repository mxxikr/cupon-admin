package com.mxxikr.couponadmin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * 쿠폰 발급 파일 업로드 요청 DTO
 * 파일 업로드를 통한 쿠폰 발급 요청 시 사용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "쿠폰 발급 파일 업로드 요청")
public class CouponIssuanceUploadRequestDTO {
    
    @NotNull(message = "파일은 필수입니다")
    @Schema(description = "업로드할 CSV 또는 Excel 파일", requiredMode = RequiredMode.REQUIRED, type = "string", format = "binary")
    private MultipartFile file;
    
    @NotBlank(message = "쿠폰명은 필수입니다")
    @Size(max = 100, message = "쿠폰명은 100자 이하여야 합니다")
    @Schema(description = "쿠폰명 (최대 100자)", requiredMode = RequiredMode.REQUIRED, example = "신규가입쿠폰")
    private String couponName;
    
    @NotNull(message = "만료일은 필수입니다")
    @Future(message = "만료일은 미래여야 합니다")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "쿠폰 만료일시 (ISO 8601 형식: yyyy-MM-dd'T'HH:mm:ss)", requiredMode = RequiredMode.REQUIRED, example = "2025-12-31T23:59:59")
    private LocalDateTime expiresAt;
}