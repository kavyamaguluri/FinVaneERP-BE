package com.finvane.entity.orders;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "quotations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    @Builder.Default
    private String tenantId = "1";

    @Column(name = "quote_no", nullable = false)
    private String quoteNo;

    @Column(name = "party_id", nullable = false)
    private Long partyId;

    @Column(name = "quote_date", nullable = false)
    private String quoteDate;

    @Column(name = "valid_until")
    private String validUntil;

    @Column(name = "taxable_value", precision = 15, scale = 2)
    private BigDecimal taxableValue;

    @Column(name = "cgst", precision = 15, scale = 2)
    private BigDecimal cgst;

    @Column(name = "sgst", precision = 15, scale = 2)
    private BigDecimal sgst;

    @Column(name = "igst", precision = 15, scale = 2)
    private BigDecimal igst;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "status")
    @Builder.Default
    private String status = "draft";

    @Column(name = "terms", columnDefinition = "TEXT")
    private String terms;

    @Column(name = "created_by")
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
