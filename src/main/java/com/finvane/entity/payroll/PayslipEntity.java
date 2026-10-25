package com.finvane.entity.payroll;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "payslips")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayslipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    @Builder.Default
    private String tenantId = "1";

    @Column(name = "payroll_run_id", nullable = false)
    private Long payrollRunId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "basic", precision = 15, scale = 2)
    private BigDecimal basic;

    @Column(name = "hra", precision = 15, scale = 2)
    private BigDecimal hra;

    @Column(name = "allowances", precision = 15, scale = 2)
    private BigDecimal allowances;

    @Column(name = "gross", precision = 15, scale = 2)
    private BigDecimal gross;

    @Column(name = "pf", precision = 15, scale = 2)
    private BigDecimal pf;

    @Column(name = "tds", precision = 15, scale = 2)
    private BigDecimal tds;

    @Column(name = "net_pay", precision = 15, scale = 2)
    private BigDecimal netPay;
}
