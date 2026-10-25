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
public class PayrollReportDTO {
    private List<MonthlyPayrollSummary> monthly;
    private List<EmployeePayrollSummary> byEmployee;
    private GrandPayrollSummary grandTotal;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyPayrollSummary {
        private Integer periodMonth;
        private Integer periodYear;
        private String status;
        private Long headcount;
        private BigDecimal totalGross;
        private BigDecimal totalPf;
        private BigDecimal totalTds;
        private BigDecimal totalNet;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeePayrollSummary {
        private Long id;
        private String name;
        private String designation;
        private String gender;
        private Long monthsPaid;
        private BigDecimal totalGross;
        private BigDecimal totalPf;
        private BigDecimal totalTds;
        private BigDecimal totalNet;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GrandPayrollSummary {
        private Long headcount;
        private BigDecimal totalGross;
        private BigDecimal totalPf;
        private BigDecimal totalTds;
        private BigDecimal totalNet;
    }
}
