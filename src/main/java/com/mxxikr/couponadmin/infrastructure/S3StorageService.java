package com.mxxikr.couponadmin.infrastructure;

import com.mxxikr.couponadmin.application.port.out.StorageService;
import com.mxxikr.couponadmin.common.constants.StorageConstants;
import com.mxxikr.couponadmin.common.exception.BusinessException;
import com.mxxikr.couponadmin.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;

/**
 * AWS S3를 사용하는 StorageService 구현체
 */
@Slf4j
@Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "aws.s3.bucket-name", matchIfMissing = false)
public class S3StorageService implements StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;

    public S3StorageService(
            @Value("${aws.s3.bucket-name}") String bucketName,
            @Value("${aws.s3.region:us-east-1}") String region,
            @Value("${aws.s3.endpoint-url:}") String endpointUrl,
            @Value("${aws.access-key-id:}") String accessKeyId,
            @Value("${aws.secret-access-key:}") String secretAccessKey) {
        
        this.bucketName = bucketName;

        // HTTP 클라이언트 명시적 지정을 위한 시스템 프로퍼티 설정
        if (System.getProperty("software.amazon.awssdk.http.service.impl") == null) {
            System.setProperty("software.amazon.awssdk.http.service.impl", 
                    "software.amazon.awssdk.http.urlconnection.UrlConnectionSdkHttpService");
        }

        // AWS 자격 증명 설정
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                accessKeyId.isEmpty() ? StorageConstants.DEFAULT_LOCALSTACK_ACCESS_KEY : accessKeyId,
                secretAccessKey.isEmpty() ? StorageConstants.DEFAULT_LOCALSTACK_SECRET_KEY : secretAccessKey
        );

        // S3 클라이언트 빌드
        // LocalStack 사용 시 path-style access 강제 (virtual-hosted-style 방지)
        S3Configuration s3Config = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();
        
        var s3ClientBuilder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .httpClient(UrlConnectionHttpClient.builder().build())
                .serviceConfiguration(s3Config);
        
        if (!endpointUrl.isEmpty()) {
            java.net.URI endpointUri = java.net.URI.create(endpointUrl);
            s3ClientBuilder.endpointOverride(endpointUri);
            log.info("S3 클라이언트 설정: endpointUrl={}, endpointUri={}, pathStyleAccessEnabled=true, bucketName={}", 
                    endpointUrl, endpointUri, bucketName);
        } else {
            log.info("S3 클라이언트 설정: endpointUrl 없음, pathStyleAccessEnabled=true");
        }
        this.s3Client = s3ClientBuilder.build();

        // S3 Presigner 빌드
        var presignerBuilder = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials));
        if (!endpointUrl.isEmpty()) {
            presignerBuilder.endpointOverride(java.net.URI.create(endpointUrl));
            // S3Presigner는 forcePathStyle을 직접 지원하지 않지만,
            // endpointOverride 설정으로 LocalStack과 호환됨
        }
        this.s3Presigner = presignerBuilder.build();

        // 버킷 존재 여부 확인 후 없으면 생성
        createBucketIfNotExists();
    }

    /**
     * 버킷이 존재하지 않으면 생성함 (로컬 개발 환경용)
     */
    private void createBucketIfNotExists() {
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            s3Client.headBucket(headBucketRequest);
            log.debug("S3 버킷 확인 완료: bucketName={}", bucketName);
        } catch (NoSuchBucketException e) {
            // 버킷 없으면 생성
            log.info("S3 버킷이 존재하지 않아 생성 시도: bucketName={}", bucketName);
            try {
                CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                        .bucket(bucketName)
                        .build();
                s3Client.createBucket(createBucketRequest);
                log.info("S3 버킷 생성 완료: bucketName={}", bucketName);
            } catch (Exception ex) {
                log.error("S3 버킷 생성 실패: bucketName={}, error={}", bucketName, ex.getMessage(), ex);
                throw new BusinessException(ErrorCode.DIRECTORY_CREATION_FAILED, ex);
            }
        } catch (Exception e) {
            log.warn("S3 버킷 확인 중 오류 발생: bucketName={}, error={}",
                    bucketName, e.getMessage());
        }
    }

    @Override
    public String store(MultipartFile file) {
        String fileKey = generateFileKey(file.getOriginalFilename());
        
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return fileKey;
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.FILE_STORAGE_FAILED, ex);
        }
    }

    @Override
    public Resource loadAsResource(String fileKey) {
        try {
            GetObjectRequest getObjectRequest = buildGetObjectRequest(fileKey);

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(StorageConstants.DEFAULT_PRESIGNED_URL_EXPIRATION_MINUTES))
                    .getObjectRequest(getObjectRequest)
                    .build();
            
            URL url = s3Presigner.presignGetObject(presignRequest).url();

            return new UrlResource(url);
        } catch (NoSuchKeyException e) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.FILE_READ_FAILED, ex);
        }
    }
    
    /**
     * GetObjectRequest를 빌드함
     * @param fileKey 파일 키
     * @return GetObjectRequest
     */
    private GetObjectRequest buildGetObjectRequest(String fileKey) {
        return GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();
    }

    @Override
    public PresignedUrlResult generateUploadPresignedUrl(String fileName, int expirationMinutes) {
        String fileKey = generateFileKey(fileName);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expirationMinutes))
                .putObjectRequest(putObjectRequest)
                .build();
        
        URL url = s3Presigner.presignPutObject(presignRequest).url();

        return new PresignedUrlResult(url, fileKey);
    }

    @Override
    public URL generateDownloadPresignedUrl(String fileKey, int expirationMinutes) {
        GetObjectRequest getObjectRequest = buildGetObjectRequest(fileKey);

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expirationMinutes))
                .getObjectRequest(getObjectRequest)
                .build();
        
        return s3Presigner.presignGetObject(presignRequest).url();
    }

    @Override
    public InputStream downloadAsStream(String fileKey) {
        try {
            GetObjectRequest getObjectRequest = buildGetObjectRequest(fileKey);
            return s3Client.getObject(getObjectRequest);
        } catch (NoSuchKeyException e) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.FILE_READ_FAILED, ex);
        }
    }

    /**
     * 파일 키를 생성함
     * @param originalFileName 원본 파일명
     * @return 생성된 파일 키
     */
    private String generateFileKey(String originalFileName) {
        String uuid = UUID.randomUUID().toString();
        String fileName = originalFileName != null ? originalFileName : StorageConstants.DEFAULT_FILENAME;
        return uuid + StorageConstants.FILE_KEY_SEPARATOR + fileName;
    }
}