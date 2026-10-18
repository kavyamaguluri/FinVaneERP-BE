package com.arcsetu.v2.dao.accounts;

import com.arcsetu.v2.entity.accounts.VoucherItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IVoucherItemJpaAccess extends JpaRepository<VoucherItemEntity, Long> {

    List<VoucherItemEntity> findByVoucherId(Long voucherId);

    @Query("SELECT vi FROM VoucherItemEntity vi JOIN vi.voucher v " +
           "WHERE v.tenantId = :tenantId AND vi.ledgerId = :ledgerId " +
           "AND (:startDate IS NULL OR v.voucherDate >= :startDate) " +
           "AND (:endDate IS NULL OR v.voucherDate <= :endDate) " +
           "ORDER BY v.voucherDate ASC, v.id ASC")
    List<VoucherItemEntity> findLedgerTransactions(@Param("tenantId") String tenantId,
                                                  @Param("ledgerId") Long ledgerId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);
}
