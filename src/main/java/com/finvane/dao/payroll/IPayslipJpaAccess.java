package com.finvane.dao.payroll;

import com.finvane.entity.payroll.PayslipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPayslipJpaAccess extends JpaRepository<PayslipEntity, Long> {

    List<PayslipEntity> findByTenantIdAndPayrollRunId(String tenantId, Long payrollRunId);

    List<PayslipEntity> findByTenantId(String tenantId);

    void deleteByTenantIdAndPayrollRunId(String tenantId, Long payrollRunId);
}
