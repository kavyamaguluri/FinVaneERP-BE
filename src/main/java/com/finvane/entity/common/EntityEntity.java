package com.finvane.entity.common;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "entities", indexes = {
    @Index(name = "idx_entity_tenant", columnList = "tenant_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, unique = true, length = 64)
    private String tenantId;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "legal_name", length = 150)
    private String legalName;

    @Column(name = "sector", length = 64)
    private String sector;

    @Column(name = "business_type", length = 32)
    @Builder.Default
    private String businessType = "goods";

    @Column(name = "pan", length = 32)
    private String pan;

    @Column(name = "gst_registered")
    @Builder.Default
    private Boolean gstRegistered = true;

    @Column(name = "gstin", length = 32)
    private String gstin;

    @Column(name = "constitution", length = 64)
    private String constitution;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "state_code", length = 32)
    private String stateCode;

    @Column(name = "country", length = 64)
    @Builder.Default
    private String country = "India";

    @Column(name = "bank_name", length = 128)
    private String bankName;

    @Column(name = "bank_account_name", length = 128)
    private String bankAccountName;

    @Column(name = "bank_account_no", length = 64)
    private String bankAccountNo;

    @Column(name = "bank_ifsc", length = 32)
    private String bankIfsc;

    @Column(name = "bank_branch", length = 128)
    private String bankBranch;

    @Column(name = "upi_id", length = 128)
    private String upiId;

    @Column(name = "date_of_commencement", length = 32)
    private String dateOfCommencement;

    @Column(name = "books_start_date", length = 32)
    private String booksStartDate;

    @Column(name = "fy_start_month")
    @Builder.Default
    private Integer fyStartMonth = 4;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
