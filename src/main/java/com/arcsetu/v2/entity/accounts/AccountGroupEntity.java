package com.arcsetu.v2.entity.accounts;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "account_groups", indexes = {
    @Index(name = "idx_account_group_tenant", columnList = "tenant_id"),
    @Index(name = "idx_account_group_name", columnList = "tenant_id, name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountGroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    @Builder.Default
    private String tenantId = "1";

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "nature", nullable = false, length = 32)
    private String nature; // ASSET, LIABILITY, EQUITY, INCOME, EXPENSE

    @Column(name = "group_level", nullable = false)
    @Builder.Default
    private Integer groupLevel = 1; // 1 = Primary, 2 = Secondary Sub-group

    @Column(name = "schedule_iii_line", length = 128)
    private String scheduleIiiLine;

    @Column(name = "is_builtin", nullable = false)
    @Builder.Default
    private Boolean isBuiltin = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
