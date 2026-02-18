package com.shopping.main.domain.product.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.shopping.main.domain.product.entity.ProductImage;
import com.shopping.main.domain.product.repository.ProductImageRepository;
import com.shopping.main.global.service.FileService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductImgService {

    @Value("${uploadPath:${UPLOAD_PATH:/app/upload/}}")
    private String uploadPath;

    private final ProductImageRepository productImageRepository;
    private final FileService fileService;

    /**
     * 이미지 저장
     * 1. 파일 디스크에 저장
     * 2. 엔티티 정보 설정
     * 3. DB 저장
     */
    public void saveProductImg(ProductImage productImage, MultipartFile itemImgFile) throws Exception {
        if (itemImgFile == null || itemImgFile.isEmpty()) {
            return;
        }

        String oriImgName = itemImgFile.getOriginalFilename();
        String imgName = "";
        String imgUrl = "";

        if (StringUtils.hasText(oriImgName)) {
            imgName = fileService.uploadFile(uploadPath, oriImgName, itemImgFile.getBytes());
            imgUrl = "/images/" + imgName;
            productImage.updateProductImg(oriImgName, imgName, imgUrl);

            productImageRepository.save(productImage);
        }

    }

    public void updateProductImg(Long productImgId, MultipartFile itemImgFile) throws Exception {

        if (!itemImgFile.isEmpty()) {

            ProductImage savedProductImg = productImageRepository.findById(productImgId)
                    .orElseThrow(EntityNotFoundException::new);

            if (StringUtils.hasText(savedProductImg.getImgName())) {
                fileService.deleteFile(uploadPath + "/" + savedProductImg.getImgName());
            }

            String oriImgName = itemImgFile.getOriginalFilename();

            String imgName = fileService.uploadFile(uploadPath, oriImgName, itemImgFile.getBytes());

            String imgUrl = "/images/" + imgName;

            savedProductImg.updateProductImg(oriImgName, imgName, imgUrl);
        }
    }
}
