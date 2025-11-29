package com.mxxikr.couponadmin.infrastructure;

import com.mxxikr.couponadmin.application.port.out.StorageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * StorageService 설정
 * S3StorageService를 기본 StorageService로 사용함
 * 로컬 개발 환경에서는 LocalStack을 통해 S3를 사용함
 */
@Configuration
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "aws.s3.bucket-name")
public class StorageServiceConfig {

    @Bean
    @Primary
    public StorageService storageService(S3StorageService s3StorageService) {
        return s3StorageService;
    }
}

