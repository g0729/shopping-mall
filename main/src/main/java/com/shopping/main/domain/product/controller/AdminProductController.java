package com.shopping.main.domain.product.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.shopping.main.domain.product.dto.ProductFormDto;
import com.shopping.main.domain.product.dto.ProductSearchDto;
import com.shopping.main.domain.product.entity.Product;
import com.shopping.main.domain.product.service.ProductService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminProductController {
    private final ProductService productService;

    // 상품 등록 페이지 보여주기
    @GetMapping(value = "/products/new")
    public String productForm(Model model) {
        model.addAttribute("productFormDto", new ProductFormDto());
        return "product/productForm";
    }

    // 상품 등록 처리하기
    @PostMapping(value = "/products/new")
    public String productNew(@Valid ProductFormDto productFormDto,
            BindingResult bindingResult,
            Model model,
            @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList) {

        // A. 유효성 검사 실패 시 (예: 상품명 누락) -> 다시 입력 폼으로 돌려보냄
        if (bindingResult.hasErrors()) {
            return "product/productForm";
        }

        // B. 첫 번째 이미지(대표 이미지)가 없는 경우 체크
        if (itemImgFileList.get(0).isEmpty() && productFormDto.getId() == null) {
            model.addAttribute("errorMessage", "첫번째 상품 이미지는 필수 입력 값 입니다.");
            return "product/productForm";
        }

        // C. 실제 저장 로직 수행
        try {
            productService.saveProduct(productFormDto, itemImgFileList);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "상품 등록 중 에러가 발생하였습니다.");
            e.printStackTrace();
            return "product/productForm";
        }

        return "redirect:/"; // 성공 시 메인 페이지로 이동
    }

    @GetMapping(value = "/products")
    public String productManage(ProductSearchDto productSearchDto,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(page, 9);
        Page<Product> productList = productService.getAdminProductPage(productSearchDto, pageable);

        model.addAttribute("items", productList);
        model.addAttribute("productSearchDto", productSearchDto);
        model.addAttribute("maxPage", 5);

        return "product/productMng";
    }

    @GetMapping("/products/{productId}")
    public String productDtl(@PathVariable(value = "productId") Long productId, Model model) {

        try {
            ProductFormDto productFormDto = productService.getAdminProductDtl(productId);
            model.addAttribute("productFormDto", productFormDto);

        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "존재하지 않는 상품입니다");
            model.addAttribute("productFormDto", new ProductFormDto());
            return "product/productForm";
        }

        return "product/productForm";
    }

    @PostMapping("/products/{productId}")
    public String productUpdate(@PathVariable(value = "productId") Long productId, @Valid ProductFormDto productFormDto,
            BindingResult bindingResult, @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "product/productForm";
        }

        productFormDto.setId(productId);

        try {
            productService.updateProduct(productFormDto, itemImgFileList);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "상품 수정 중 에러가 발생했습니다");
            return "product/productForm";
        }

        return "redirect:/admin/products";
    }
}
