package com.shopping.main.global.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileService {

    /**
     * 파일 업로드 처리
     * 
     * @param uploadPath       : 파일을 저장할 실제 경로
     * @param originalFileName : 사용자가 올린 원본 파일명 (예: image.jpg)
     * @param fileData         : 파일의 바이트 데이터
     * @return savedFileName : 서버에 저장된 유일한 파일명 (UUID 적용)
     */

    public String uploadFile(String uploadPath, String originalFilename, byte[] fileData) throws Exception {
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

    /**
     * 파일 삭제 처리
     */

    public void deleteFile(String filePath) throws Exception {
        File deleteFile = new File(filePath);

        if (deleteFile.exists()) {
            if (!deleteFile.delete()) {
                throw new IOException("파일 삭제에 실패했습니다: " + filePath);
            }
            log.info("{} 파일을 삭제하였습니다.", filePath);
        } else {
            log.info("{} 파일이 존재하지 않습니다.", filePath);
        }
    }
}
