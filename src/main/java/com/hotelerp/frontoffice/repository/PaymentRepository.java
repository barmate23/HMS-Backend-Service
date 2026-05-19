package com.hotelerp.frontoffice.repository;

import com.hotelerp.frontoffice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBill_Id(Long billId);
}
