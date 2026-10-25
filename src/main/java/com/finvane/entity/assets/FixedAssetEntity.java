package com.finvane.entity.assets;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fixed_assets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FixedAssetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    @Builder.Default
    private String tenantId = "1";

    @Column(name = "asset_code", nullable = false)
    private String assetCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "category")
    private String category;

    @Column(name = "department")
    private String department;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "purchase_date")
    private String purchaseDate;

    @Column(name = "date_put_to_use")
    private String datePutToUse;

    @Column(name = "cost", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal cost = BigDecimal.ZERO;

    @Column(name = "salvage_value", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal salvageValue = BigDecimal.ZERO;

    @Column(name = "useful_life_years", precision = 15, scale = 2)
    private BigDecimal usefulLifeYears;

    @Column(name = "depreciation_method")
    @Builder.Default
    private String depreciationMethod = "SLM";

    @Column(name = "accumulated_depreciation", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal accumulatedDepreciation = BigDecimal.ZERO;

    @Column(name = "status")
    @Builder.Default
    private String status = "active";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
