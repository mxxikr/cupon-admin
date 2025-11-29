package com.mxxikr.couponadmin.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * S3 업로드 완료 후 처리 요청 DTO
 * 클라이언트가 S3에 파일 업로드를 완료한 후 서버에 알릴 때 사용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class S3UploadCompleteRequestDTO {
    
    @NotBlank(message = "파일 키는 필수입니다")
    private String fileKey;
    
    @NotBlank(message = "원본 파일명은 필수입니다")
    @Size(max = 255, message = "원본 파일명은 255자 이하여야 합니다")
    private String originalFileName;
    
    @NotBlank(message = "쿠폰명은 필수입니다")
    @Size(max = 100, message = "쿠폰명은 100자 이하여야 합니다")
    private String couponName;
    
    @NotNull(message = "만료일은 필수입니다")
    @Future(message = "만료일은 미래여야 합니다")
    private LocalDateTime expiresAt;
}

