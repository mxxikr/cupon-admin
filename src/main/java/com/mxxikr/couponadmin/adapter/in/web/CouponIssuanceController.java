package com.mxxikr.couponadmin.adapter.in.web;

import com.mxxikr.couponadmin.application.CouponIssuanceService;
import com.mxxikr.couponadmin.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 쿠폰 발급 관련 API 요청을 처리
 */
@Tag(name = "쿠폰 대량 발급 API", description = "파일을 이용한 쿠폰 대량 발급 관련 API")
@RestController
@RequestMapping("/api/coupon-issuances")
@RequiredArgsConstructor
@Validated
public class CouponIssuanceController {

    private final CouponIssuanceService couponIssuanceService;

    @Operation(
            summary = "쿠폰 발급 파일 업로드", 
            description = "CSV 또는 Excel 형식의 사용자 목록 파일을 업로드하여 쿠폰 발급을 비동기로 요청"
    )
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CouponIssuanceResponseDTO> uploadFile(
            @Parameter(description = "업로드할 CSV 또는 Excel 파일", required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("file") @NotNull(message = "파일은 필수입니다") MultipartFile file,
            @Parameter(description = "쿠폰명 (최대 100자)", required = true, example = "신규가입쿠폰")
            @RequestParam("couponName") @NotBlank(message = "쿠폰명은 필수입니다") @Size(max = 100, message = "쿠폰명은 100자 이하여야 합니다") String couponName,
            @Parameter(description = "쿠폰 만료일시 (ISO 8601 형식: yyyy-MM-dd'T'HH:mm:ss)", required = true, example = "2025-12-31T23:59:59")
            @RequestParam("expiresAt") @NotNull(message = "만료일은 필수입니다") @Future(message = "만료일은 미래여야 합니다") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime expiresAt) {
        String jobId = couponIssuanceService.uploadFileAndIssueCoupons(
                file, couponName, expiresAt);

        return ResponseEntity.status(HttpStatus.CREATED).body(new CouponIssuanceResponseDTO(jobId));
    }

    @Operation(summary = "업로드된 파일 다운로드", description = "업로드 시 반환된 파일 ID를 사용하여 원본 파일을 다운로드 (스트리밍 방식)")
    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
        FileDownloadResourceDTO fileDto = couponIssuanceService.downloadFileAsResource(fileId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        
        String encodedFileName = URLEncoder.encode(fileDto.originalFileName(), StandardCharsets.UTF_8).replace("+", "%20");
        headers.setContentDispositionFormData("attachment", encodedFileName);

        return ResponseEntity.ok().headers(headers).body(fileDto.resource());
    }

    @Operation(summary = "파일 업로드용 Presigned URL 생성", description = "S3에 직접 업로드하기 위한 Presigned URL을 생성함")
    @PostMapping("/presigned-url/upload")
    public ResponseEntity<PresignedUrlResponseDTO> generateUploadPresignedUrl(
            @Valid @ModelAttribute PresignedUrlUploadRequestDTO request) {
        var result = couponIssuanceService.generateUploadPresignedUrl(
                request.getFileName(), request.getExpirationMinutes());

        return ResponseEntity.ok(new PresignedUrlResponseDTO(result.presignedUrl().toString(), result.fileKey()));
    }

    @Operation(summary = "S3 업로드 완료 후 파일 처리", description = "클라이언트가 S3에 파일 업로드를 완료한 후 서버에서 파일을 파싱하여 쿠폰을 비동기로 발급함")
    @PostMapping("/s3-upload-complete")
    public ResponseEntity<CouponIssuanceResponseDTO> processS3UploadedFile(
            @Valid @ModelAttribute S3UploadCompleteRequestDTO request) {
        String jobId = couponIssuanceService.processS3UploadedFile(
                request.getFileKey(), request.getOriginalFileName(), 
                request.getCouponName(), request.getExpiresAt());

        return ResponseEntity.status(HttpStatus.CREATED).body(new CouponIssuanceResponseDTO(jobId));
    }

    @Operation(summary = "쿠폰 발급 작업 상태 조회", description = "쿠폰 발급 작업의 현재 상태를 조회함")
    @GetMapping("/jobs/{jobId}/status")
    public ResponseEntity<CouponIssuanceJobStatusDTO> getJobStatus(@PathVariable String jobId) {
        CouponIssuanceJobStatusDTO status = couponIssuanceService.getJobStatus(jobId);
        return ResponseEntity.ok(status);
    }

    @Operation(summary = "파일 다운로드용 Presigned URL 생성", description = "S3에서 직접 다운로드하기 위한 Presigned URL을 생성함")
    @GetMapping("/{fileId}/presigned-url/download")
    public ResponseEntity<PresignedUrlResponseDTO> generateDownloadPresignedUrl(
            @PathVariable String fileId,
            @RequestParam(value = "expirationMinutes", defaultValue = "60") int expirationMinutes) {
        URL presignedUrl = couponIssuanceService.generateDownloadPresignedUrl(fileId, expirationMinutes);

        return ResponseEntity.ok(new PresignedUrlResponseDTO(presignedUrl.toString(), null));
    }
}
