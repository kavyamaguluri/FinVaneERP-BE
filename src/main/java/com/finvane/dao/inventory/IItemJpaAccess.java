package com.finvane.dao.inventory;

import com.finvane.entity.inventory.ItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IItemJpaAccess extends JpaRepository<ItemEntity, Long> {

    List<ItemEntity> findByTenantIdAndIsActiveOrderByNameAsc(String tenantId, Long isActive);

    List<ItemEntity> findByTenantIdAndItemTypeAndIsActiveOrderByNameAsc(String tenantId, String itemType, Long isActive);

    List<ItemEntity> findByTenantIdOrderByNameAsc(String tenantId);
}
