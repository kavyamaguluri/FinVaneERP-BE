package com.finvane.entity.payroll;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollRunEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    @Builder.Default
    private String tenantId = "1";

    @Column(name = "period_month", nullable = false)
    private Integer periodMonth;

    @Column(name = "period_year", nullable = false)
    private Integer periodYear;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "draft"; // 'draft' or 'processed'

    @Column(name = "voucher_id")
    private Long voucherId;

    @Column(name = "processed_by")
    private Long processedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
