package com.finvane.controller.banking;

import com.finvane.dao.banking.IBankAccountJpaAccess;
import com.finvane.dao.banking.IBankTransactionJpaAccess;
import com.finvane.entity.banking.BankAccountEntity;
import com.finvane.entity.banking.BankTransactionEntity;
import com.finvane.model.common.ApiResponse;
import com.finvane.model.common.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
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

    @PutMapping("/accounts/{id}")
    public ApiResponse<BankAccountEntity> updateBankAccount(@PathVariable Long id, @RequestBody BankAccountEntity entity) {
        String tenantId = TenantContext.getTenantId();
        BankAccountEntity existing = bankAccountDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bank account not found with ID: " + id));
        existing.setName(entity.getName());
        if (entity.getAccountNoEnc() != null && !entity.getAccountNoEnc().isBlank()) {
            existing.setAccountNoEnc(entity.getAccountNoEnc());
        }
        existing.setIfsc(entity.getIfsc());
        existing.setOpeningBalance(entity.getOpeningBalance());
        existing.setAccountId(entity.getAccountId());
        return ApiResponse.ok("Bank account updated successfully", bankAccountDao.save(existing));
    }

    @DeleteMapping("/accounts/{id}")
    @Transactional
    public ApiResponse<Void> deleteBankAccount(@PathVariable Long id) {
        String tenantId = TenantContext.getTenantId();
        BankAccountEntity existing = bankAccountDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bank account not found with ID: " + id));
        List<BankTransactionEntity> txns = bankTransactionDao.findByTenantIdAndBankAccountIdOrderByTxnDateDesc(tenantId, id);
        if (!txns.isEmpty()) {
            throw new IllegalStateException("\"" + existing.getName() + "\" has " + txns.size() + " transaction(s). Delete those first, then delete the account.");
        }
        bankAccountDao.delete(existing);
        return ApiResponse.ok("Bank account deleted successfully", null);
    }

    @GetMapping("/transactions")
    public ApiResponse<List<BankTransactionEntity>> getTransactions(@RequestParam(required = false) Long accountId) {
        String tenantId = TenantContext.getTenantId();
        if (accountId != null) {
            return ApiResponse.ok(bankTransactionDao.findByTenantIdAndBankAccountIdOrderByTxnDateDesc(tenantId, accountId));
        }
        return ApiResponse.ok(bankTransactionDao.findByTenantIdOrderByTxnDateDesc(tenantId));
    }

    @PostMapping("/transactions")
    public ApiResponse<BankTransactionEntity> createTransaction(@RequestBody BankTransactionEntity entity) {
        String tenantId = TenantContext.getTenantId();
        entity.setTenantId(tenantId);
        return ApiResponse.ok("Transaction added successfully", bankTransactionDao.save(entity));
    }

    @PostMapping("/transactions/{id}/reconcile")
    public ApiResponse<BankTransactionEntity> reconcileTransaction(@PathVariable Long id) {
        BankTransactionEntity txn = bankTransactionDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with ID: " + id));
        txn.setReconciled(1L);
        return ApiResponse.ok("Transaction marked as reconciled", bankTransactionDao.save(txn));
    }

    @DeleteMapping("/transactions/{id}")
    public ApiResponse<Void> deleteTransaction(@PathVariable Long id) {
        bankTransactionDao.deleteById(id);
        return ApiResponse.ok("Transaction deleted successfully", null);
    }

    @DeleteMapping("/transactions/delete-all")
    @Transactional
    public ApiResponse<Void> deleteAllTransactions(@RequestParam Long accountId) {
        String tenantId = TenantContext.getTenantId();
        List<BankTransactionEntity> txns = bankTransactionDao.findByTenantIdAndBankAccountIdOrderByTxnDateDesc(tenantId, accountId);
        bankTransactionDao.deleteAll(txns);
        return ApiResponse.ok("All transactions deleted for account", null);
    }
}
