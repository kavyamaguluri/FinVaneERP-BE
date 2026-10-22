package com.finvane.dao.common;

import com.finvane.entity.common.EntityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IEntityJpaAccess extends JpaRepository<EntityEntity, Long> {

    Optional<EntityEntity> findByTenantIdAndIsActiveTrue(String tenantId);

    List<EntityEntity> findByIsActiveTrueOrderByNameAsc();
}
