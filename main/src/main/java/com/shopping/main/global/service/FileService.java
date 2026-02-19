package com.shopping.main.global.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Profile("!prod")
@Slf4j
public class FileService implements FileStorageService {

    @Value("${uploadPath:${UPLOAD_PATH:/app/upload/}}")
    private String uploadPath;

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

        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists() && !uploadDir.mkdirs()) {
            throw new IOException("업로드 경로 생성에 실패했습니다: " + uploadPath);
        }
        if (!uploadDir.isDirectory()) {
            throw new IOException("업로드 경로가 디렉토리가 아닙니다: " + uploadPath);
        }

        UUID uuid = UUID.randomUUID();
        String extension = originalFilename.substring(dotIndex);
        String saveFileName = uuid.toString() + extension;
        File saveFile = new File(uploadDir, saveFileName);

        try (FileOutputStream fos = new FileOutputStream(saveFile)) {
            fos.write(fileData);
        }

        return saveFileName;
    }

    @Override
    public void deleteFile(String fileKey) throws Exception {
        if (fileKey == null || fileKey.isBlank()) {
            return;
        }
        File deleteFile = new File(uploadPath, fileKey);
        if (deleteFile.exists()) {
            if (!deleteFile.delete()) {
                throw new IOException("파일 삭제에 실패했습니다: " + fileKey);
            }
            log.info("{} 파일을 삭제하였습니다.", fileKey);
        } else {
            log.info("{} 파일이 존재하지 않습니다.", fileKey);
        }
    }

    @Override
    public String resolveImgUrl(String imgName) {
        return "/images/" + imgName;
    }
}
