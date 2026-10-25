package com.finvane.dao.assets;

import com.finvane.entity.assets.FixedAssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IFixedAssetJpaAccess extends JpaRepository<FixedAssetEntity, Long> {
    List<FixedAssetEntity> findByTenantIdOrderByNameAsc(String tenantId);
    long countByTenantId(String tenantId);
}
