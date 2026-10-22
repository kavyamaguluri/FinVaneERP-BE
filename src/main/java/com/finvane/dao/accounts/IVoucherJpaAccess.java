package com.finvane.dao.accounts;

import com.finvane.entity.accounts.VoucherEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface IVoucherJpaAccess extends JpaRepository<VoucherEntity, Long> {

    Optional<VoucherEntity> findByTenantIdAndVoucherNumber(String tenantId, String voucherNumber);

    @Query("SELECT v FROM VoucherEntity v WHERE v.tenantId = :tenantId AND " +
           "(:voucherType IS NULL OR v.voucherType = :voucherType) AND " +
           "(:startDate IS NULL OR v.voucherDate >= :startDate) AND " +
           "(:endDate IS NULL OR v.voucherDate <= :endDate) " +
           "ORDER BY v.voucherDate DESC, v.id DESC")
    Page<VoucherEntity> searchVouchers(@Param("tenantId") String tenantId,
                                      @Param("voucherType") String voucherType,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate,
                                      Pageable pageable);

    @Query("SELECT COUNT(v) FROM VoucherEntity v WHERE v.tenantId = :tenantId AND v.voucherType = :voucherType")
    long countByTenantIdAndVoucherType(@Param("tenantId") String tenantId, @Param("voucherType") String voucherType);
}
