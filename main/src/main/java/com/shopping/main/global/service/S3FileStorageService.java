package com.shopping.main.global.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Profile("prod")
@Slf4j
@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Override
    public String uploadFile(String originalFilename, byte[] fileData) throws Exception {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("원본 파일명이 비어 있습니다.");
        }
        if (fileData == null || fileData.length == 0) {
            throw new IllegalArgumentException("업로드할 파일 데이터가 비어 있습니다.");
        }

        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            throw new IllegalArgumentException("확장자가 없는 파일은 업로드할 수 없습니다.");
        }

        String extension = originalFilename.substring(dotIndex);
        String key = UUID.randomUUID() + extension;

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(resolveContentType(extension))
                .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(fileData));
        log.info("S3 업로드 완료: {}", key);
        return key;
    }

    @Override
    public void deleteFile(String fileKey) throws Exception {
        if (fileKey == null || fileKey.isBlank()) {
            return;
        }
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .build());
        log.info("S3 파일 삭제 완료: {}", fileKey);
    }

    @Override
    public String resolveImgUrl(String imgName) {
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + imgName;
    }

    private String resolveContentType(String extension) {
        return switch (extension.toLowerCase()) {
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".gif" -> "image/gif";
            case ".webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }
}
