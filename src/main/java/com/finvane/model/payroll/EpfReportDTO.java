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
public class EpfReportDTO {
    private String monthYear;
    private Long employeeCount;
    private BigDecimal totalWages;
    private BigDecimal totalEeShare;   // 12%
    private BigDecimal totalErShare;   // 3.67%
    private BigDecimal totalEpsShare;  // 8.33%
    private BigDecimal totalEpfRemittance;
    private List<EpfEmployeeRow> rows;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EpfEmployeeRow {
        private Long employeeId;
        private String employeeName;
        private String uan;
        private BigDecimal grossWages;
        private BigDecimal epfWages;
        private BigDecimal epsWages;
        private BigDecimal edliWages;
        private BigDecimal eeShare;
        private BigDecimal erShare;
        private BigDecimal epsShare;
    }
}
