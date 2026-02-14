package com.shopping.main.domain.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shopping.main.domain.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByImpUid(String impUid);

    Optional<Payment> findByOrderId(Long orderId);

    // 주문 ID 목록으로 결제 정보 일괄 조회
    @Query("select p from Payment p where p.order.id in :orderIds")
    List<Payment> findByOrdersIds(@Param("orderIds") List<Long> orderIds);
}
