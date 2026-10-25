package com.finvane.dao.orders;

import com.finvane.entity.orders.QuotationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IQuotationJpaAccess extends JpaRepository<QuotationEntity, Long> {
    List<QuotationEntity> findByTenantIdOrderByIdDesc(String tenantId);
    long countByTenantId(String tenantId);
}
