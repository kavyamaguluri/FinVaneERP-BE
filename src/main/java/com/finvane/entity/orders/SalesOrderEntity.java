package com.finvane.entity.orders;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    @Builder.Default
    private String tenantId = "1";

    @Column(name = "so_no", nullable = false)
    private String soNo;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "so_date", nullable = false)
    private String soDate;

    @Column(name = "expected_date")
    private String expectedDate;

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
    private String status = "open";

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by")
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
