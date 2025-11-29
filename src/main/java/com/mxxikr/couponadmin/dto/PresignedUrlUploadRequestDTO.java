package com.mxxikr.couponadmin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Presigned URL 업로드 요청 DTO
 * S3 업로드용 Presigned URL 생성 요청 시 사용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUrlUploadRequestDTO {
    
    @NotBlank(message = "파일명은 필수입니다")
    @Size(max = 255, message = "파일명은 255자 이하여야 합니다")
    private String fileName;
    
    @Min(value = 1, message = "만료 시간은 최소 1분이어야 합니다")
    @Max(value = 1440, message = "만료 시간은 최대 1440분(24시간)이어야 합니다")
    private Integer expirationMinutes = 60;
}