package com.finvane.entity.inventory;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "direct_stock_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DirectStockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    @Builder.Default
    private String tenantId = "1";

    @Column(name = "entry_date", nullable = false)
    private String entryDate;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "valuation_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal valuationAmount;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by")
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
