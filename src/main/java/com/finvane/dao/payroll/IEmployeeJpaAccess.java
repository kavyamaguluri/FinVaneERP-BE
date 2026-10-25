package com.finvane.dao.payroll;

import com.finvane.entity.payroll.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IEmployeeJpaAccess extends JpaRepository<EmployeeEntity, Long> {

    List<EmployeeEntity> findByTenantIdAndIsActiveOrderByNameAsc(String tenantId, Long isActive);

    List<EmployeeEntity> findByTenantIdOrderByNameAsc(String tenantId);
}
