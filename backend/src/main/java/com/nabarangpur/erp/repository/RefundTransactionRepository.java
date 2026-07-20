package com.nabarangpur.erp.repository;

import com.nabarangpur.erp.entity.RefundTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface RefundTransactionRepository extends JpaRepository<RefundTransaction, Long> {
	

    @Transactional
    void deleteByPaymentId(Long paymentId);
}
