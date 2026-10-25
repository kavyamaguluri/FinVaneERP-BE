package com.finvane.entity.production;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "bom_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BomLineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    @Builder.Default
    private String tenantId = "1";

    @Column(name = "bom_id", nullable = false)
    private Long bomId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "qty", precision = 15, scale = 4)
    private BigDecimal qty;

    @Column(name = "unit_cost", precision = 15, scale = 2)
    private BigDecimal unitCost;
}
