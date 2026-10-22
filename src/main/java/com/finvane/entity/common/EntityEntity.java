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
    @Builder.Default
    private String tenantId = "1";

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "legal_name", length = 150)
    private String legalName;

    @Column(name = "sector", length = 64)
    private String sector;

    @Column(name = "business_type", length = 32)
    @Builder.Default
    private String businessType = "goods";

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
