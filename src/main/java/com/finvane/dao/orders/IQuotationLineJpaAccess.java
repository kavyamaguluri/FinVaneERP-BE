package com.finvane.dao.orders;

import com.finvane.entity.orders.QuotationLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IQuotationLineJpaAccess extends JpaRepository<QuotationLineEntity, Long> {
    List<QuotationLineEntity> findByTenantIdAndQuotationId(String tenantId, Long quotationId);
    void deleteByTenantIdAndQuotationId(String tenantId, Long quotationId);
}
