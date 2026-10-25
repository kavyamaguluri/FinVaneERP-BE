package com.finvane.dao.banking;

import com.finvane.entity.banking.BankAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IBankAccountJpaAccess extends JpaRepository<BankAccountEntity, Long> {
    List<BankAccountEntity> findByTenantIdOrderByNameAsc(String tenantId);
}
