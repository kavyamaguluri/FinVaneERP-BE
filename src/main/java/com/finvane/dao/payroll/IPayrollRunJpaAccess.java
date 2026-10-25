package com.finvane.dao.payroll;

import com.finvane.entity.payroll.PayrollRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPayrollRunJpaAccess extends JpaRepository<PayrollRunEntity, Long> {

    List<PayrollRunEntity> findByTenantIdOrderByPeriodYearDescPeriodMonthDesc(String tenantId);

    Optional<PayrollRunEntity> findByTenantIdAndPeriodMonthAndPeriodYear(String tenantId, Integer periodMonth, Integer periodYear);
}
