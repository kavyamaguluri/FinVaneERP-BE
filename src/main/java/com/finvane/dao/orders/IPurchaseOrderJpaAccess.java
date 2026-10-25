package com.finvane.dao.orders;

import com.finvane.entity.orders.PurchaseOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPurchaseOrderJpaAccess extends JpaRepository<PurchaseOrderEntity, Long> {
    List<PurchaseOrderEntity> findByTenantIdOrderByIdDesc(String tenantId);
    long countByTenantId(String tenantId);
}
