package com.finvane.entity.inventory;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    @Builder.Default
    private String tenantId = "1";

    @Column(name = "sku")
    private String sku;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "item_type")
    @Builder.Default
    private String itemType = "goods"; // 'goods' or 'service'

    @Column(name = "category")
    private String category;

    @Column(name = "brand")
    private String brand;

    @Column(name = "hsn_sac")
    private String hsnSac;

    @Column(name = "uom")
    private String uom;

    @Column(name = "purchase_price", precision = 15, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "sale_price", precision = 15, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "gst_rate", precision = 15, scale = 2)
    private BigDecimal gstRate;

    @Column(name = "opening_stock", precision = 15, scale = 2)
    private BigDecimal openingStock;

    @Column(name = "reorder_level", precision = 15, scale = 2)
    private BigDecimal reorderLevel;

    @Column(name = "item_class")
    private String itemClass; // 'raw_material', 'wip', 'finished_good'

    @Column(name = "is_active")
    @Builder.Default
    private Long isActive = 1L;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
