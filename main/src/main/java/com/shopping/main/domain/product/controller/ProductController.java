package com.shopping.main.domain.product.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.shopping.main.domain.product.dto.ProductDtlDto;
import com.shopping.main.domain.product.service.ProductService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;

    @GetMapping(value = "/{productId}")
    public String productDtl(@PathVariable("productId") Long productId, Model model) {
        ProductDtlDto productDtlDto = productService.getProductDtlView(productId);

        model.addAttribute("product", productDtlDto);

        return "product/productDtl";
    }

}
