package com.finvane.entity.production;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "boms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    @Builder.Default
    private String tenantId = "1";

    @Column(name = "bom_code", nullable = false)
    private String bomCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "output_item_id", nullable = false)
    private Long outputItemId;

    @Column(name = "batch_size", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal batchSize = BigDecimal.ONE;

    @Column(name = "labour_cost", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal labourCost = BigDecimal.ZERO;

    @Column(name = "overhead_mode")
    @Builder.Default
    private String overheadMode = "per_batch";

    @Column(name = "overhead_rate", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal overheadRate = BigDecimal.ZERO;

    @Column(name = "is_active")
    @Builder.Default
    private Long isActive = 1L;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
