package com.finvane.entity.invoices;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "invoice_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceLineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    @Builder.Default
    private String tenantId = "1";

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "qty", precision = 15, scale = 2)
    private BigDecimal qty;

    @Column(name = "rate", precision = 15, scale = 2)
    private BigDecimal rate;

    @Column(name = "taxable_value", precision = 15, scale = 2)
    private BigDecimal taxableValue;

    @Column(name = "gst_rate", precision = 15, scale = 2)
    private BigDecimal gstRate;

    @Column(name = "cgst", precision = 15, scale = 2)
    private BigDecimal cgst;

    @Column(name = "sgst", precision = 15, scale = 2)
    private BigDecimal sgst;

    @Column(name = "igst", precision = 15, scale = 2)
    private BigDecimal igst;

    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "description")
    private String description;

    @Column(name = "hsn_sac")
    private String hsnSac;
}
