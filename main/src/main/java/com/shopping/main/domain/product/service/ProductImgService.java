package com.shopping.main.domain.product.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.shopping.main.domain.product.entity.ProductImage;
import com.shopping.main.domain.product.repository.ProductImageRepository;
import com.shopping.main.global.service.FileStorageService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductImgService {

    private final ProductImageRepository productImageRepository;
    private final FileStorageService fileStorageService;

    /**
     * 이미지 저장
     * 1. 파일 업로드 (로컬 or S3, 프로필에 따라 자동 선택)
     * 2. 엔티티 정보 설정
     * 3. DB 저장
     */
    public void saveProductImg(ProductImage productImage, MultipartFile itemImgFile) throws Exception {
        if (itemImgFile == null || itemImgFile.isEmpty()) {
            return;
        }

        String oriImgName = itemImgFile.getOriginalFilename();
        if (StringUtils.hasText(oriImgName)) {
            String imgName = fileStorageService.uploadFile(oriImgName, itemImgFile.getBytes());
            String imgUrl = fileStorageService.resolveImgUrl(imgName);
            productImage.updateProductImg(oriImgName, imgName, imgUrl);
            productImageRepository.save(productImage);
        }
    }

    public void updateProductImg(Long productImgId, MultipartFile itemImgFile) throws Exception {
        if (!itemImgFile.isEmpty()) {
            ProductImage savedProductImg = productImageRepository.findById(productImgId)
                    .orElseThrow(EntityNotFoundException::new);

            if (StringUtils.hasText(savedProductImg.getImgName())) {
                fileStorageService.deleteFile(savedProductImg.getImgName());
            }

            String oriImgName = itemImgFile.getOriginalFilename();
            String imgName = fileStorageService.uploadFile(oriImgName, itemImgFile.getBytes());
            String imgUrl = fileStorageService.resolveImgUrl(imgName);
            savedProductImg.updateProductImg(oriImgName, imgName, imgUrl);
        }
    }
}
