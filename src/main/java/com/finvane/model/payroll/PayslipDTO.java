package com.finvane.model.payroll;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayslipDTO {
    private Long id;
    private Long payrollRunId;
    private Long employeeId;
    private String employeeName;
    private String designation;
    private BigDecimal basic;
    private BigDecimal hra;
    private BigDecimal allowances;
    private BigDecimal gross;
    private BigDecimal pf;
    private BigDecimal tds;
    private BigDecimal netPay;
}
