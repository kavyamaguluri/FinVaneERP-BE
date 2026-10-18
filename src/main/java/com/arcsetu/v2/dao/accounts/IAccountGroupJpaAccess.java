package com.arcsetu.v2.dao.accounts;

import com.arcsetu.v2.entity.accounts.AccountGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IAccountGroupJpaAccess extends JpaRepository<AccountGroupEntity, Long> {

    List<AccountGroupEntity> findByTenantIdOrderByNameAsc(String tenantId);

    List<AccountGroupEntity> findByTenantIdAndGroupLevelOrderByNameAsc(String tenantId, Integer groupLevel);

    Optional<AccountGroupEntity> findByTenantIdAndName(String tenantId, String name);

    boolean existsByTenantIdAndName(String tenantId, String name);
}
