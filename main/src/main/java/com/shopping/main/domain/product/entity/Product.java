package com.shopping.main.domain.product.entity;

import java.util.ArrayList;
import java.util.List;

import com.shopping.main.domain.product.constant.ProductSellStatus;
import com.shopping.main.domain.product.dto.ProductFormDto;
import com.shopping.main.domain.product.exception.OutOfStockException;
import com.shopping.main.global.common.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "product")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int stockQuantity; // 재고 수량

    @Lob
    @Column(nullable = false)
    private String description; // 상품 상세 설명

    @Enumerated(EnumType.STRING)
    private ProductSellStatus productSellStatus;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductImage> productImages = new ArrayList<>();

    public void updateProduct(ProductFormDto productFormDto) {

        this.name = productFormDto.getName();
        this.price = productFormDto.getPrice();

        this.stockQuantity = productFormDto.getStockQuantity();

        this.description = productFormDto.getDescription();

        this.productSellStatus = productFormDto.getProductSellStatus();
    }

    public void removeStock(int stockNumber) {
        int restStock = this.stockQuantity - stockNumber;

        if (restStock < 0) {
            throw new OutOfStockException("상품의 재고가 부족합니다. (현재 재고 수량: " + this.stockQuantity + ")");
        }
        this.stockQuantity = restStock;
    }

    public void addStock(int stockNumber) {
        this.stockQuantity += stockNumber;
    }
}
