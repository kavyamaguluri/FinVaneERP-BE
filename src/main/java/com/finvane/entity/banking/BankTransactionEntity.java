package com.finvane.entity.banking;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    @Builder.Default
    private String tenantId = "1";

    @Column(name = "bank_account_id", nullable = false)
    private Long bankAccountId;

    @Column(name = "txn_date", nullable = false)
    private String txnDate;

    @Column(name = "description")
    private String description;

    @Column(name = "debit", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal debit = BigDecimal.ZERO;

    @Column(name = "credit", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal credit = BigDecimal.ZERO;

    @Column(name = "reconciled")
    @Builder.Default
    private Long reconciled = 0L;

    @Column(name = "matched_voucher_id")
    private Long matchedVoucherId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
