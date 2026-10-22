package com.finvane.entity.common;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_name", length = 128)
    private String entityName;

    @Column(name = "user_email", length = 128)
    private String userEmail;

    @Column(name = "action", nullable = false, length = 64)
    private String action;

    @Column(name = "entity", length = 64)
    private String entity;

    @Column(name = "entity_id", length = 64)
    private String entityId;

    @Column(name = "ip", length = 64)
    @Builder.Default
    private String ip = "::ffff:127.0.0.1";

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
