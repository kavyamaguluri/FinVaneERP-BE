package com.finvane.dao.invoices;

import com.finvane.entity.invoices.InvoiceLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IInvoiceLineJpaAccess extends JpaRepository<InvoiceLineEntity, Long> {

    List<InvoiceLineEntity> findByTenantIdAndInvoiceId(String tenantId, Long invoiceId);

    void deleteByTenantIdAndInvoiceId(String tenantId, Long invoiceId);
}
