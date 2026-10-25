package com.finvane.dao.production;

import com.finvane.entity.production.ProductionOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IProductionOrderJpaAccess extends JpaRepository<ProductionOrderEntity, Long> {
    List<ProductionOrderEntity> findByTenantIdOrderByIdDesc(String tenantId);
    long countByTenantId(String tenantId);
}
