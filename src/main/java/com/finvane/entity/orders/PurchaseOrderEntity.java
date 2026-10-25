package com.finvane.entity.orders;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    @Builder.Default
    private String tenantId = "1";

    @Column(name = "po_no", nullable = false)
    private String poNo;

    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "po_date")
    private String poDate;

    @Column(name = "expected_date")
    private String expectedDate;

    @Column(name = "status")
    @Builder.Default
    private String status = "open";

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

    @Column(name = "invoice_id")
    private Long invoiceId;

    @Column(name = "created_by")
    private Long createdBy;
}
