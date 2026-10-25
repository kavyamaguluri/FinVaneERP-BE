package com.finvane.entity.assets;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset_depreciation_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetDepreciationRunEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    @Builder.Default
    private String tenantId = "1";

    @Column(name = "period_month", nullable = false)
    private Integer periodMonth;

    @Column(name = "period_year", nullable = false)
    private Integer periodYear;

    @Column(name = "voucher_id")
    private Long voucherId;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "run_by")
    private Long runBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
