package com.finvane.model.dashboard;

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
    private BigDecimal netWorkingCapital;
    private BigDecimal netProfit;
    private BigDecimal totalSales;
    private BigDecimal totalPurchases;
    private BigDecimal otherExpenses;
    private BigDecimal grossProfit;
    private BigDecimal cashRunwayMonths;
    private long totalLedgers;
    private long totalParties;
    private long totalVouchers;
    private List<String> chartMonths;
    private List<BigDecimal> monthlySales;
    private List<BigDecimal> monthlyExpenses;
    private List<BigDecimal> receivablesAging;
    private List<BigDecimal> payablesAging;
    private long headcountNow;
    private BigDecimal periodicalPayrollCost;
    private long maleCount;
    private long femaleCount;
}
