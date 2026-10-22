package com.finvane.entity.accounts;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vouchers", indexes = {
    @Index(name = "idx_voucher_tenant", columnList = "tenant_id"),
    @Index(name = "idx_voucher_no", columnList = "tenant_id, voucher_number"),
    @Index(name = "idx_voucher_date", columnList = "tenant_id, voucher_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    @Builder.Default
    private String tenantId = "1";

    @Column(name = "voucher_number", nullable = false, length = 64)
    private String voucherNumber;

    @Column(name = "voucher_type", nullable = false, length = 32)
    private String voucherType; // JOURNAL, PAYMENT, RECEIPT, CONTRA

    @Column(name = "voucher_date", nullable = false)
    private LocalDate voucherDate;

    @Column(name = "narration", columnDefinition = "TEXT")
    private String narration;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "reference_no", length = 64)
    private String referenceNo;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<VoucherItemEntity> items = new ArrayList<>();
}
