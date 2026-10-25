package com.finvane.dao.budgets;

import com.finvane.entity.budgets.BudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IBudgetJpaAccess extends JpaRepository<BudgetEntity, Long> {

    List<BudgetEntity> findByTenantIdAndPeriodStartAndPeriodEnd(String tenantId, String periodStart, String periodEnd);

    List<BudgetEntity> findByTenantId(String tenantId);

    Optional<BudgetEntity> findByTenantIdAndAccountIdAndPeriodStartAndPeriodEnd(String tenantId, Long accountId, String periodStart, String periodEnd);

    void deleteByTenantIdAndAccountIdAndPeriodStartAndPeriodEnd(String tenantId, Long accountId, String periodStart, String periodEnd);
}
