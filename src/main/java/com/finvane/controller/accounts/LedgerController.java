package com.finvane.controller.accounts;

import com.finvane.model.accounts.LedgerStatementDTO;
import com.finvane.model.accounts.TrialBalanceRowDTO;
import com.finvane.model.common.ApiResponse;
import com.finvane.service.accounts.ILedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v2/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final ILedgerService ledgerService;

    @GetMapping("/statement/{ledgerId}")
    public ApiResponse<LedgerStatementDTO> getStatement(
            @PathVariable Long ledgerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ApiResponse.ok(ledgerService.getLedgerStatement(ledgerId, fromDate, toDate));
    }

    @GetMapping("/trial-balance")
    public ApiResponse<TrialBalanceRowDTO.Report> getTrialBalance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        return ApiResponse.ok(ledgerService.getTrialBalance(asOfDate));
    }
}
