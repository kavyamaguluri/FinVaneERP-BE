package com.finvane.model.payroll;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollRunDTO {
    private Long id;
    private Integer periodMonth;
    private Integer periodYear;
    private String status;
    private Long voucherId;
    private Long processedBy;
    private List<PayslipDTO> payslips;
    private BigDecimal totalGross;
    private BigDecimal totalPf;
    private BigDecimal totalTds;
    private BigDecimal totalNet;
    private Integer employeeCount;
}
