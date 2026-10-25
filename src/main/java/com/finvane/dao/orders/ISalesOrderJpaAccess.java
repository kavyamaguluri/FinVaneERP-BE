package com.finvane.dao.orders;

import com.finvane.entity.orders.SalesOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ISalesOrderJpaAccess extends JpaRepository<SalesOrderEntity, Long> {
    List<SalesOrderEntity> findByTenantIdOrderByIdDesc(String tenantId);
    long countByTenantId(String tenantId);
}
