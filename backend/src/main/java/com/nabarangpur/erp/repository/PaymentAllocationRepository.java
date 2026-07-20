package com.nabarangpur.erp.repository;

import com.nabarangpur.erp.entity.PaymentAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PaymentAllocationRepository extends JpaRepository<PaymentAllocation, Long> {
    List<PaymentAllocation> findByPaymentId(Long paymentId);
    
    @Transactional
    void deleteByPaymentId(Long paymentId);
}
