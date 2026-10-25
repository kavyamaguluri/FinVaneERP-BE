package com.finvane.dao.invoices;

import com.finvane.entity.invoices.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IInvoiceJpaAccess extends JpaRepository<InvoiceEntity, Long> {

    List<InvoiceEntity> findByTenantIdAndInvoiceTypeOrderByIdDesc(String tenantId, String invoiceType);

    List<InvoiceEntity> findByTenantIdOrderByIdDesc(String tenantId);

    long countByTenantIdAndInvoiceType(String tenantId, String invoiceType);

    Optional<InvoiceEntity> findByTenantIdAndInvoiceNo(String tenantId, String invoiceNo);
}
