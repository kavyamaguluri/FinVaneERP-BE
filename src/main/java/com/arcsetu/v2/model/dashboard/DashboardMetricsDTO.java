package com.arcsetu.v2.model.dashboard;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardMetricsDTO {
    private BigDecimal cashBankBalance;
    private BigDecimal totalReceivables;
    private BigDecimal totalPayables;
    private BigDecimal netProfit;
    private long totalLedgers;
    private long totalParties;
    private long totalVouchers;
    private List<String> chartMonths;
    private List<BigDecimal> monthlySales;
    private List<BigDecimal> monthlyExpenses;
}
