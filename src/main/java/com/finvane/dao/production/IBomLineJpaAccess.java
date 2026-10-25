package com.finvane.dao.production;

import com.finvane.entity.production.BomLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IBomLineJpaAccess extends JpaRepository<BomLineEntity, Long> {
    List<BomLineEntity> findByTenantIdAndBomId(String tenantId, Long bomId);
    void deleteByTenantIdAndBomId(String tenantId, Long bomId);
}
