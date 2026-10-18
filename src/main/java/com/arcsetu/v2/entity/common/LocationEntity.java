package com.arcsetu.v2.entity.common;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "locations", indexes = {
    @Index(name = "idx_loc_tenant", columnList = "tenant_id"),
    @Index(name = "idx_loc_code", columnList = "tenant_id, code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    @Builder.Default
    private String tenantId = "1";

    @Column(name = "code", nullable = false, length = 32)
    private String code;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "location_type", nullable = false, length = 32)
    @Builder.Default
    private String locationType = "warehouse"; // warehouse, branch, office, retail_store

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "state_code", nullable = false, length = 2)
    private String stateCode;

    @Column(name = "gstin", length = 15)
    private String gstin;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
