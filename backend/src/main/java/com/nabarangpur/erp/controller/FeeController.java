package com.nabarangpur.erp.controller;

import com.nabarangpur.erp.dto.common.ApiResponse;
import com.nabarangpur.erp.dto.fee.*;
import com.nabarangpur.erp.service.FeeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fees")
@RequiredArgsConstructor
@Tag(name = "Fee Management", description = "Functional Spec 6.5 - fee structure, payments, dues, receipts")
public class FeeController {

    private final FeeService feeService;

    @PostMapping("/invoices")
    @PreAuthorize("hasAuthority('FEES_CREATE')")
    public ApiResponse<FeeInvoiceResponse> generateInvoice(@Valid @RequestBody GenerateInvoiceRequest request) {
        return ApiResponse.ok("Invoice generated", feeService.generateInvoice(request));
    }

    @PostMapping("/payments")
    @PreAuthorize("hasAuthority('FEES_CREATE')")
    public ApiResponse<PaymentReceiptResponse> recordPayment(@Valid @RequestBody RecordPaymentRequest request) {
        return ApiResponse.ok("Payment recorded and receipt generated", feeService.recordPayment(request));
    }

    @PostMapping("/payments/{paymentId}/reverse")
    @PreAuthorize("hasAuthority('FEES_DELETE')")
    public ApiResponse<Void> reversePayment(@PathVariable Long paymentId, @Valid @RequestBody ReverseClassPaymentRequest request) {
        feeService.reversePayment(paymentId, request);
        return ApiResponse.message("Payment reversed");
    }

    @PostMapping("/waivers")
    @PreAuthorize("hasAuthority('FEES_UPDATE')")
    public ApiResponse<Void> applyWaiver(@Valid @RequestBody FeeWaiverRequest request) {
        feeService.applyWaiver(request);
        return ApiResponse.message("Fee waiver applied");
    }

    @GetMapping("/students/{studentId}/invoices")
    @PreAuthorize("hasAuthority('FEES_READ')")
    public ApiResponse<List<FeeInvoiceResponse>> invoicesForStudent(@PathVariable Long studentId) {
        return ApiResponse.ok(feeService.invoicesForStudent(studentId));
    }

    @GetMapping("/dues")
    @PreAuthorize("hasAuthority('FEES_READ')")
    public ApiResponse<List<FeeInvoiceResponse>> dueList() {
        return ApiResponse.ok(feeService.dueList());
    }

    @GetMapping("/paid")
    @PreAuthorize("hasAuthority('FEES_READ')")
    public ApiResponse<List<FeeInvoiceResponse>> paidList() {
        return ApiResponse.ok(feeService.paidList());
    }

    @GetMapping("/collections/today")
    @PreAuthorize("hasAuthority('FEES_READ')")
    public ApiResponse<Map<String, BigDecimal>> todaysCollection() {
        return ApiResponse.ok(Map.of("totalCollectedToday", feeService.totalCollectionToday()));
    }
}
