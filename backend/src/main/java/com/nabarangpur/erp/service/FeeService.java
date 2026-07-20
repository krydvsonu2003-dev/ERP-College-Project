package com.nabarangpur.erp.service;
import java.time.Instant;
import com.nabarangpur.erp.dto.fee.*;
import com.nabarangpur.erp.entity.*;
import com.nabarangpur.erp.exception.BadRequestException;
import com.nabarangpur.erp.exception.ConflictException;
import com.nabarangpur.erp.exception.InvalidWorkflowStateException;
import com.nabarangpur.erp.exception.ResourceNotFoundException;
import com.nabarangpur.erp.repository.*;
import com.nabarangpur.erp.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeeService {

    private final FeeStructureRepository feeStructureRepository;
    private final StudentFeeAssignmentRepository studentFeeAssignmentRepository;
    private final FeeInvoiceRepository feeInvoiceRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;
    private final ReceiptRepository receiptRepository;
    private final RefundTransactionRepository refundTransactionRepository;
    private final FeeWaiverRepository feeWaiverRepository;
    private final StudentRepository studentRepository;
    private final AuditService auditService;

    @Transactional
    public FeeInvoiceResponse generateInvoice(GenerateInvoiceRequest req) {
        Student student = studentRepository.findById(req.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (feeInvoiceRepository.findByStudentIdAndAcademicYearIdAndSemester(
                student.getId(), req.getAcademicYearId(), req.getSemester()).isPresent()) {
            throw new ConflictException("An invoice already exists for this student/year/semester");
        }

        List<FeeStructure> structures = feeStructureRepository.findByCourseIdAndAcademicYearIdAndSemester(
                student.getCourse().getId(), req.getAcademicYearId(), req.getSemester());
        System.out.println("Structures Size = " + structures.size());
//        System.out.println("Student Category = " + student.getCategory());
//       
        // Fee structure may vary by category; fall back to GENERAL if no category-specific row exists.
        List<FeeStructure> applicable = structures.stream()
                .filter(s -> s.getCategory().equalsIgnoreCase("GENERAL")
                        || (student.getCategory() != null && s.getCategory().equalsIgnoreCase(student.getCategory())))
                .collect(Collectors.toList());
        System.out.println("Applicable = " + applicable.size());
        if (applicable.isEmpty()) {
            throw new BadRequestException("No fee structure configured for this course/year/semester");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (FeeStructure fs : applicable) {
            BigDecimal waived = BigDecimal.ZERO;
            var assignment = studentFeeAssignmentRepository.findByStudentId(student.getId()).stream()
                    .filter(a -> a.getFeeStructure().getId().equals(fs.getId())).findFirst()
                    .orElseGet(() -> studentFeeAssignmentRepository.save(StudentFeeAssignment.builder()
                            .student(student).feeStructure(fs).applicableAmount(fs.getAmount()).waivedAmount(BigDecimal.ZERO)
                            .assignedAt(Instant.now()).build()));
            waived = assignment.getWaivedAmount() == null ? BigDecimal.ZERO : assignment.getWaivedAmount();
            total = total.add(fs.getAmount().subtract(waived));
        }

        FeeInvoice invoice = FeeInvoice.builder()
        		 .invoiceNumber(generateInvoiceNumber())
                .student(student)
                .academicYear(structures.get(0).getAcademicYear())
                .semester(req.getSemester())
                .totalAmount(total)
                .paidAmount(BigDecimal.ZERO)
                .dueAmount(total)
                .status(FeeInvoiceStatus.DUE)
                .dueDate(req.getDueDate())
                .build();
        invoice = feeInvoiceRepository.save(invoice);

        auditService.record("FEE_INVOICE_GENERATE", "FeeInvoice", invoice.getId(), null, total);
        return FeeInvoiceResponse.from(invoice);
    }

    @Transactional
    public PaymentReceiptResponse recordPayment(RecordPaymentRequest req) {
        FeeInvoice invoice = feeInvoiceRepository.findById(req.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        if (req.getAmount().compareTo(invoice.getDueAmount()) > 0) {
            throw new BadRequestException("Payment amount exceeds the outstanding due amount of " + invoice.getDueAmount());
        }

        User cashier = SecurityUtils.currentDomainUser();
        Payment payment = Payment.builder()
                .invoice(invoice)
                .student(invoice.getStudent())
                .amount(req.getAmount())
                .paymentMode(req.getPaymentMode())
                .paymentReference(req.getPaymentReference())
                .status(PaymentStatus.POSTED)
                .receivedBy(cashier)
                .build();
        payment = paymentRepository.save(payment);

        allocateAcrossFeeHeads(payment, invoice);

        // Partial payment must reduce outstanding balance correctly.
        invoice.setPaidAmount(invoice.getPaidAmount().add(req.getAmount()));
        invoice.setDueAmount(invoice.getTotalAmount().subtract(invoice.getPaidAmount()));
        invoice.setStatus(invoice.getDueAmount().compareTo(BigDecimal.ZERO) <= 0
                ? FeeInvoiceStatus.PAID
                : FeeInvoiceStatus.PARTIALLY_PAID);
        feeInvoiceRepository.save(invoice);

        String receiptNumber = generateReceiptNumber();
        Receipt receipt = Receipt.builder()
                .payment(payment)
                .receiptNumber(receiptNumber)
                .generatedBy(cashier)
                .build();
        receiptRepository.save(receipt);

        auditService.record("FEE_PAYMENT_COLLECT", "Payment", payment.getId(), null, req.getAmount());

        return PaymentReceiptResponse.builder()
                .paymentId(payment.getId())
                .receiptNumber(receiptNumber)
                .studentId(invoice.getStudent().getId())
                .studentName(invoice.getStudent().getFullName())
                .studentCode(invoice.getStudent().getStudentCode())
                .amount(req.getAmount())
                .paymentMode(req.getPaymentMode().name())
                .paymentReference(req.getPaymentReference())
                .invoiceDueAmountAfter(invoice.getDueAmount())
                .invoiceStatusAfter(invoice.getStatus().name())
                .paidAt(payment.getPaidAt())
                .receivedByName(cashier.getFullName())
                .build();
    }

    private void allocateAcrossFeeHeads(Payment payment, FeeInvoice invoice) {
        List<StudentFeeAssignment> assignments = studentFeeAssignmentRepository.findByStudentId(invoice.getStudent().getId());
        BigDecimal totalApplicable = assignments.stream()
                .map(a -> a.getApplicableAmount().subtract(a.getWaivedAmount() == null ? BigDecimal.ZERO : a.getWaivedAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalApplicable.compareTo(BigDecimal.ZERO) == 0 || assignments.isEmpty()) return;

        for (StudentFeeAssignment a : assignments) {
            BigDecimal net = a.getApplicableAmount().subtract(a.getWaivedAmount() == null ? BigDecimal.ZERO : a.getWaivedAmount());
            BigDecimal share = payment.getAmount().multiply(net).divide(totalApplicable, 2, RoundingMode.HALF_UP);
            if (share.compareTo(BigDecimal.ZERO) > 0) {
                paymentAllocationRepository.save(PaymentAllocation.builder()
                        .payment(payment)
                        .feeHead(a.getFeeStructure().getFeeHead())
                        .allocatedAmount(share)
                        .build());
            }
        }
    }

    /** All payment transactions are immutable after posting, except via authorized reversal. */
    @Transactional
    public void reversePayment(Long paymentId, ReverseClassPaymentRequest req) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        if (payment.getStatus() == PaymentStatus.REVERSED) {
            throw new InvalidWorkflowStateException("Payment has already been reversed");
        }

        FeeInvoice invoice = payment.getInvoice();
        invoice.setPaidAmount(invoice.getPaidAmount().subtract(payment.getAmount()));
        invoice.setDueAmount(invoice.getTotalAmount().subtract(invoice.getPaidAmount()));
        invoice.setStatus(invoice.getPaidAmount().compareTo(BigDecimal.ZERO) <= 0
                ? FeeInvoiceStatus.DUE
                : FeeInvoiceStatus.PARTIALLY_PAID);
        feeInvoiceRepository.save(invoice);

        payment.setStatus(PaymentStatus.REVERSED);
        paymentRepository.save(payment);

        refundTransactionRepository.save(RefundTransaction.builder()
                .payment(payment)
                .refundAmount(payment.getAmount())
                .reason(req.getReason())
                .processedBy(SecurityUtils.currentDomainUser())
                .build());

        auditService.record("FEE_PAYMENT_REVERSE", "Payment", payment.getId(), payment.getAmount(), req.getReason());
    }

    @Transactional
    public void applyWaiver(FeeWaiverRequest req) {
        StudentFeeAssignment assignment = studentFeeAssignmentRepository.findById(req.getStudentFeeAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student fee assignment not found"));

        BigDecimal newWaived = (assignment.getWaivedAmount() == null ? BigDecimal.ZERO : assignment.getWaivedAmount())
                .add(req.getWaiverAmount());
        if (newWaived.compareTo(assignment.getApplicableAmount()) > 0) {
            throw new BadRequestException("Waiver amount cannot exceed the applicable fee amount");
        }
        assignment.setWaivedAmount(newWaived);
        studentFeeAssignmentRepository.save(assignment);

        feeWaiverRepository.save(FeeWaiver.builder()
                .studentFeeAssignment(assignment)
                .waiverAmount(req.getWaiverAmount())
                .reason(req.getReason())
                .approvedBy(SecurityUtils.currentDomainUser())
                .build());

        auditService.record("FEE_WAIVER_APPLY", "StudentFeeAssignment", assignment.getId(), null, req.getWaiverAmount());
    }

    @Transactional(readOnly = true)
    public List<FeeInvoiceResponse> invoicesForStudent(Long studentId) {
        enforceSelfAccessIfStudent(studentId);
        return feeInvoiceRepository.findByStudentId(studentId).stream()
                .map(FeeInvoiceResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeeInvoiceResponse> dueList() {
        return feeInvoiceRepository.findAll().stream()
                .filter(i -> !i.getStudent().isDeleted())
                .filter(i -> i.getStatus() != FeeInvoiceStatus.PAID)
                .map(FeeInvoiceResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeeInvoiceResponse> paidList() {
        return feeInvoiceRepository.findAll().stream()
                .filter(i -> !i.getStudent().isDeleted())
                .filter(i -> i.getStatus() == FeeInvoiceStatus.PAID)
                .map(FeeInvoiceResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BigDecimal totalCollectionToday() {
        var today = LocalDate.now();
        return paymentRepository.findPostedBetween(
                        today.atStartOfDay(java.time.ZoneOffset.UTC).toInstant(),
                        today.plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC).toInstant())
                .stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void enforceSelfAccessIfStudent(Long studentId) {
        if (SecurityUtils.isStudent()) {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
            Long currentUserId = SecurityUtils.currentUserId();
            if (student.getUser() == null || !student.getUser().getId().equals(currentUserId)) {
                throw new BadRequestException("Students may only view their own fee dues");
            }
        }
    }
    private String generateInvoiceNumber() {

        String year = String.valueOf(LocalDate.now().getYear());

        long seq = feeInvoiceRepository.count() + 1;

        String invoiceNo = "INV-" + year + "-" + String.format("%06d", seq);

        while (feeInvoiceRepository.existsByInvoiceNumber(invoiceNo)) {
            seq++;
            invoiceNo = "INV-" + year + "-" + String.format("%06d", seq);
        }

        return invoiceNo;
    }
    private String generateReceiptNumber() {
        String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
        long seq = receiptRepository.count() + 1;
        String candidate = "RCPT-" + year + "-" + String.format("%06d", seq);
        while (receiptRepository.existsByReceiptNumber(candidate)) {
            seq++;
            candidate = "RCPT-" + year + "-" + String.format("%06d", seq);
        }
        return candidate;
    }
}
