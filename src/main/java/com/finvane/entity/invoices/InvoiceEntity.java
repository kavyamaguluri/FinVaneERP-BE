package com.finvane.entity.invoices;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    @Builder.Default
    private String tenantId = "1";

    @Column(name = "invoice_no", nullable = false)
    private String invoiceNo;

    @Column(name = "invoice_type", nullable = false)
    private String invoiceType; // 'sales' or 'purchase'

    @Column(name = "party_id", nullable = false)
    private Long partyId;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "invoice_date")
    private String invoiceDate;

    @Column(name = "due_date")
    private String dueDate;

    @Column(name = "place_of_supply")
    private String placeOfSupply;

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
    private String status = "posted";

    @Column(name = "voucher_id")
    private Long voucherId;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "irn")
    private String irn;

    @Column(name = "irn_ack_no")
    private String irnAckNo;

    @Column(name = "irn_ack_date")
    private String irnAckDate;

    @Column(name = "irn_qr", columnDefinition = "TEXT")
    private String irnQr;

    @Column(name = "ewb_no")
    private String ewbNo;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
