package com.shopping.main.domain.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shopping.main.domain.product.entity.Product;

import jakarta.persistence.LockModeType;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("select p from Product p where p.id = :id")
        Optional<Product> findByIdForUpdate(@Param("id") Long id);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("select p from Product p " +
                        "where p.id in :ids " +
                        "order by p.id")
        List<Product> findAllByIdForUpdate(@Param("ids") List<Long> ids);

        @Query("select p from Product p " +
                        "join fetch p.productImages " +
                        "where p.id = :id")
        Optional<Product> findWithImagesById(@Param("id") Long productId);
}
