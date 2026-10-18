package com.arcsetu.v2.controller.accounts;

import com.arcsetu.v2.dao.accounts.IAccountGroupJpaAccess;
import com.arcsetu.v2.entity.accounts.AccountGroupEntity;
import com.arcsetu.v2.model.accounts.AccountLedgerDTO;
import com.arcsetu.v2.model.common.ApiResponse;
import com.arcsetu.v2.model.common.PageResponse;
import com.arcsetu.v2.model.common.TenantContext;
import com.arcsetu.v2.service.accounts.ILedgerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/accounts")
@RequiredArgsConstructor
public class ChartOfAccountsController {

    private final ILedgerService ledgerService;
    private final IAccountGroupJpaAccess groupDao;

    @GetMapping("/groups")
    public ApiResponse<List<AccountGroupEntity>> getAccountGroups() {
        String tenantId = TenantContext.getTenantId();
        return ApiResponse.ok(groupDao.findByTenantIdOrderByNameAsc(tenantId));
    }

    @GetMapping("/ledgers")
    public ApiResponse<PageResponse<AccountLedgerDTO>> getLedgers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ApiResponse.ok(ledgerService.searchLedgers(search, groupId, page, size));
    }

    @GetMapping("/ledgers/all")
    public ApiResponse<List<AccountLedgerDTO>> getAllActiveLedgers() {
        return ApiResponse.ok(ledgerService.getAllActiveLedgers());
    }

    @GetMapping("/ledgers/{id}")
    public ApiResponse<AccountLedgerDTO> getLedgerById(@PathVariable Long id) {
        return ApiResponse.ok(ledgerService.getLedgerById(id));
    }

    @PostMapping("/ledgers")
    public ApiResponse<AccountLedgerDTO> createLedger(@Valid @RequestBody AccountLedgerDTO dto) {
        return ApiResponse.ok("Ledger created successfully", ledgerService.createLedger(dto));
    }
}
