package com.finvane.dao.production;

import com.finvane.entity.production.BomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IBomJpaAccess extends JpaRepository<BomEntity, Long> {
    List<BomEntity> findByTenantIdAndIsActiveOrderByNameAsc(String tenantId, Long isActive);
    long countByTenantIdAndIsActive(String tenantId, Long isActive);
}
