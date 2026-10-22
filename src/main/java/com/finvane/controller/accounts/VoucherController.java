package com.finvane.controller.accounts;

import com.finvane.model.accounts.VoucherRequestDTO;
import com.finvane.model.common.ApiResponse;
import com.finvane.model.common.PageResponse;
import com.finvane.service.accounts.IVoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v2/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final IVoucherService voucherService;

    @PostMapping
    public ApiResponse<VoucherRequestDTO> createVoucher(@Valid @RequestBody VoucherRequestDTO request) {
        return ApiResponse.ok("Voucher posted successfully", voucherService.createVoucher(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<VoucherRequestDTO> getVoucherById(@PathVariable Long id) {
        return ApiResponse.ok(voucherService.getVoucherById(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<VoucherRequestDTO>> searchVouchers(
            @RequestParam(required = false) String voucherType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(voucherService.searchVouchers(voucherType, startDate, endDate, page, size));
    }
}
