package com.finvane.dao.inventory;

import com.finvane.entity.inventory.DirectStockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDirectStockJpaAccess extends JpaRepository<DirectStockEntity, Long> {
    List<DirectStockEntity> findByTenantIdOrderByEntryDateDesc(String tenantId);
}
