package com.mxxikr.couponadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Presigned URL 응답 DTO
 * S3 업로드/다운로드를 위한 Presigned URL 생성 시 사용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUrlResponseDTO {
    private String presignedUrl;
    private String fileKey;
}