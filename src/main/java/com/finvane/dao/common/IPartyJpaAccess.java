package com.finvane.dao.common;

import com.finvane.entity.common.PartyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPartyJpaAccess extends JpaRepository<PartyEntity, Long> {

    List<PartyEntity> findByTenantIdAndIsActiveTrueOrderByNameAsc(String tenantId);

    List<PartyEntity> findByTenantIdAndPartyTypeAndIsActiveTrueOrderByNameAsc(String tenantId, String partyType);

    @Query("SELECT p FROM PartyEntity p WHERE p.tenantId = :tenantId AND " +
           "(:partyType IS NULL OR p.partyType = :partyType) AND " +
           "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.gstin) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<PartyEntity> searchParties(@Param("tenantId") String tenantId,
                                   @Param("partyType") String partyType,
                                   @Param("search") String search,
                                   Pageable pageable);

    Optional<PartyEntity> findByTenantIdAndGstin(String tenantId, String gstin);
}
