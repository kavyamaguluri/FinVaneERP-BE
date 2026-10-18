package com.arcsetu.v2.dao.common;

import com.arcsetu.v2.entity.common.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ILocationJpaAccess extends JpaRepository<LocationEntity, Long> {

    List<LocationEntity> findByTenantIdAndIsActiveTrueOrderByNameAsc(String tenantId);

    Optional<LocationEntity> findByTenantIdAndCode(String tenantId, String code);
}
