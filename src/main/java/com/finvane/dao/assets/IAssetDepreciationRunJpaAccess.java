package com.finvane.dao.assets;

import com.finvane.entity.assets.AssetDepreciationRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IAssetDepreciationRunJpaAccess extends JpaRepository<AssetDepreciationRunEntity, Long> {
    List<AssetDepreciationRunEntity> findByTenantIdOrderByPeriodYearDescPeriodMonthDesc(String tenantId);
}
