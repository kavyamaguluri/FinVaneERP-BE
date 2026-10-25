package com.finvane.dao.banking;

import com.finvane.entity.banking.BankTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IBankTransactionJpaAccess extends JpaRepository<BankTransactionEntity, Long> {
    List<BankTransactionEntity> findByTenantIdOrderByTxnDateDesc(String tenantId);
    List<BankTransactionEntity> findByTenantIdAndBankAccountIdOrderByTxnDateDesc(String tenantId, Long bankAccountId);
}
