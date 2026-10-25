package com.finvane.entity.production;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "production_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    @Builder.Default
    private String tenantId = "1";

    @Column(name = "order_no", nullable = false)
    private String orderNo;

    @Column(name = "bom_id", nullable = false)
    private Long bomId;

    @Column(name = "output_item_id", nullable = false)
    private Long outputItemId;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "planned_qty", nullable = false, precision = 15, scale = 2)
    private BigDecimal plannedQty;

    @Column(name = "produced_qty", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal producedQty = BigDecimal.ZERO;

    @Column(name = "order_date", nullable = false)
    private String orderDate;

    @Column(name = "status")
    @Builder.Default
    private String status = "planned"; // 'planned', 'in_progress', 'completed'

    @Column(name = "total_cost", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalCost = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
