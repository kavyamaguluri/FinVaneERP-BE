package com.arcsetu.v2.dao.accounts;

import com.arcsetu.v2.entity.accounts.AccountLedgerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IAccountLedgerJpaAccess extends JpaRepository<AccountLedgerEntity, Long> {

    List<AccountLedgerEntity> findByTenantIdAndIsActiveTrueOrderByNameAsc(String tenantId);

    Page<AccountLedgerEntity> findByTenantId(String tenantId, Pageable pageable);

    @Query("SELECT l FROM AccountLedgerEntity l WHERE l.tenantId = :tenantId AND " +
           "(:search IS NULL OR LOWER(l.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(l.code) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:groupId IS NULL OR l.groupId = :groupId)")
    Page<AccountLedgerEntity> searchLedgers(@Param("tenantId") String tenantId,
                                           @Param("search") String search,
                                           @Param("groupId") Long groupId,
                                           Pageable pageable);

    Optional<AccountLedgerEntity> findByTenantIdAndCode(String tenantId, String code);

    Optional<AccountLedgerEntity> findByTenantIdAndName(String tenantId, String name);
}
