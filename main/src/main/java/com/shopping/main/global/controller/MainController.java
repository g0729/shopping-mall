package com.shopping.main.global.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.shopping.main.domain.product.dto.ProductSummaryDto;
import com.shopping.main.domain.product.dto.ProductSearchDto;
import com.shopping.main.domain.product.service.ProductService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final ProductService productService;

    @GetMapping("/")
    public String index(ProductSearchDto productSearchDto, @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(page, 9);
        Page<ProductSummaryDto> items = productService.getMainProductList(productSearchDto, pageable);

        model.addAttribute("items", items);
        model.addAttribute("productSearchDto", productSearchDto); // 검색어 유지용
        model.addAttribute("maxPage", 5);

        return "index";
    }
}
