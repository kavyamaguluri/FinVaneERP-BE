package com.finvane.entity.common;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "parties", indexes = {
    @Index(name = "idx_party_tenant", columnList = "tenant_id"),
    @Index(name = "idx_party_gstin", columnList = "tenant_id, gstin"),
    @Index(name = "idx_party_type", columnList = "tenant_id, party_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    @Builder.Default
    private String tenantId = "1";

    @Column(name = "party_type", nullable = false, length = 32)
    private String partyType; // CUSTOMER, VENDOR, BOTH

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "trade_name", length = 255)
    private String tradeName;

    @Column(name = "gstin", length = 15)
    private String gstin;

    @Column(name = "pan", length = 10)
    private String pan;

    @Column(name = "state_code", nullable = false, length = 2)
    private String stateCode; // 2-digit Indian GST State code (e.g. "27" for Maharashtra)

    @Column(name = "email", length = 128)
    private String email;

    @Column(name = "phone", length = 32)
    private String phone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "opening_balance", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Column(name = "current_balance", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
