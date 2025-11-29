package com.mxxikr.couponadmin.controller;

import com.mxxikr.couponadmin.application.FileUploadService;
import com.mxxikr.couponadmin.dto.UploadResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 업로드 전용 API 컨트롤러
 * 파일 업로드만 수행하며 쿠폰 발급은 하지 않음
 */
@Tag(name = "파일 관리 API", description = "파일 업로드 및 다운로드 관련 API")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileUploadService fileUploadService;

    @Operation(
            summary = "파일 업로드 (쿠폰 발급 없음)",
            description = "CSV 또는 Excel 형식의 사용자 목록 파일을 업로드함. 쿠폰 발급은 하지 않으며 파일만 저장함."
    )
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponseDTO> uploadFile(
            @RequestParam("file") MultipartFile file) {
        String fileId = fileUploadService.uploadFile(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UploadResponseDTO(fileId));
    }
}

