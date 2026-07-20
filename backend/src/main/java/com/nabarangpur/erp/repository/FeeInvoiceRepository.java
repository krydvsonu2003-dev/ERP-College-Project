package com.nabarangpur.erp.repository;
import org.springframework.transaction.annotation.Transactional;
import com.nabarangpur.erp.entity.FeeInvoice;
import com.nabarangpur.erp.entity.FeeInvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeeInvoiceRepository extends JpaRepository<FeeInvoice, Long> {
    Optional<FeeInvoice> findByStudentIdAndAcademicYearIdAndSemester(Long studentId, Long academicYearId, Integer semester);
    List<FeeInvoice> findByStudentId(Long studentId);
    
    @Transactional
    void deleteByStudentId(Long studentId);
    Page<FeeInvoice> findByStatus(FeeInvoiceStatus status, Pageable pageable);
    long countByStatus(FeeInvoiceStatus status);
    boolean existsByInvoiceNumber(String invoiceNumber);

    Optional<FeeInvoice> findByInvoiceNumber(String invoiceNumber);
}
