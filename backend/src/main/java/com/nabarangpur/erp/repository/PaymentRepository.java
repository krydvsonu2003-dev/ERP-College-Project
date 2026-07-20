package com.nabarangpur.erp.repository;
import org.springframework.transaction.annotation.Transactional;
import com.nabarangpur.erp.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByStudentId(Long studentId);
    List<Payment> findByInvoiceId(Long invoiceId);

    @Transactional
    void deleteByStudentId(Long studentId);
    
    @Query("select p from Payment p where p.paidAt between :from and :to and p.status = 'POSTED'")
    List<Payment> findPostedBetween(Instant from, Instant to);
    
   
}
