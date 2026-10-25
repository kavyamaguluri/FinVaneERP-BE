package com.finvane.entity.banking;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    @Builder.Default
    private String tenantId = "1";

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "account_no_enc")
    private String accountNoEnc;

    @Column(name = "ifsc")
    private String ifsc;

    @Column(name = "opening_balance", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Column(name = "account_id")
    private Long accountId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
