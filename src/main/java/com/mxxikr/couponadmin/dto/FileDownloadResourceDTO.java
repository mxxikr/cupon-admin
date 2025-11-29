package com.mxxikr.couponadmin.dto;

import org.springframework.core.io.Resource;

/**
 * 파일 다운로드 시 Resource를 포함한 DTO
 * 스트리밍 방식으로 파일 다운로드 시 사용
 */
public record FileDownloadResourceDTO(String originalFileName, Resource resource) {
}