package com.finvane.dao.orders;

import com.finvane.entity.orders.SalesOrderLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ISalesOrderLineJpaAccess extends JpaRepository<SalesOrderLineEntity, Long> {
    List<SalesOrderLineEntity> findByTenantIdAndSalesOrderId(String tenantId, Long salesOrderId);
    void deleteByTenantIdAndSalesOrderId(String tenantId, Long salesOrderId);
}
