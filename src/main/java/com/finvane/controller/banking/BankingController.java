package com.finvane.controller.banking;

import com.finvane.dao.banking.IBankAccountJpaAccess;
import com.finvane.dao.banking.IBankTransactionJpaAccess;
import com.finvane.entity.banking.BankAccountEntity;
import com.finvane.entity.banking.BankTransactionEntity;
import com.finvane.model.common.ApiResponse;
import com.finvane.model.common.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/banking")
@RequiredArgsConstructor
public class BankingController {

    private final IBankAccountJpaAccess bankAccountDao;
    private final IBankTransactionJpaAccess bankTransactionDao;

    @GetMapping("/accounts")
    public ApiResponse<List<BankAccountEntity>> getBankAccounts() {
        String tenantId = TenantContext.getTenantId();
        return ApiResponse.ok(bankAccountDao.findByTenantIdOrderByNameAsc(tenantId));
    }

    @PostMapping("/accounts")
    public ApiResponse<BankAccountEntity> createBankAccount(@RequestBody BankAccountEntity entity) {
        String tenantId = TenantContext.getTenantId();
        entity.setTenantId(tenantId);
        return ApiResponse.ok("Bank account created successfully", bankAccountDao.save(entity));
    }

    @GetMapping("/transactions")
    public ApiResponse<List<BankTransactionEntity>> getTransactions(@RequestParam(required = false) Long accountId) {
        String tenantId = TenantContext.getTenantId();
        if (accountId != null) {
            return ApiResponse.ok(bankTransactionDao.findByTenantIdAndBankAccountIdOrderByTxnDateDesc(tenantId, accountId));
        }
        return ApiResponse.ok(bankTransactionDao.findByTenantIdOrderByTxnDateDesc(tenantId));
    }
}
