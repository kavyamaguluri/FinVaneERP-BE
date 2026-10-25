package com.finvane.dao.orders;

import com.finvane.entity.orders.PurchaseOrderLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPurchaseOrderLineJpaAccess extends JpaRepository<PurchaseOrderLineEntity, Long> {
    List<PurchaseOrderLineEntity> findByTenantIdAndPoId(String tenantId, Long poId);
    void deleteByTenantIdAndPoId(String tenantId, Long poId);
}
