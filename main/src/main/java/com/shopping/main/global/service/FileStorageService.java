package com.shopping.main.global.service;

public interface FileStorageService {
    String uploadFile(String originalFilename, byte[] fileData) throws Exception;
    void deleteFile(String fileKey) throws Exception;
    String resolveImgUrl(String imgName);
}
