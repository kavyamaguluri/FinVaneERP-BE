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
    private BigDecimal inventoryValue;
    private BigDecimal currentAssets;
    private BigDecimal currentLiabilities;
    private BigDecimal netProfit;
    private BigDecimal totalSales;
    private BigDecimal totalPurchases;
    private BigDecimal otherExpenses;
    private BigDecimal grossProfit;
    private BigDecimal cashRunwayMonths;
    private BigDecimal cogsInPeriod;
    private BigDecimal dso;
    private BigDecimal dpo;
    private BigDecimal dio;
    private BigDecimal c2c;
    private BigDecimal currentRatio;
    private BigDecimal quickRatio;
    private BigDecimal goodsRevenue;
    private BigDecimal serviceRevenue;
    private Long goodsQtySold;
    private Integer periodDays;
    private Long totalLedgers;
    private Long totalParties;
    private Long totalVouchers;
    private Integer pendingApprovals;
    private List<String> chartMonths;
    private List<BigDecimal> monthlySales;
    private List<BigDecimal> monthlyExpenses;
    private List<BigDecimal> receivablesAging;
    private List<BigDecimal> payablesAging;
    private List<BigDecimal> inventoryAging;
    private Long headcountNow;
    private BigDecimal avgHeadcountPeriod;
    private BigDecimal attritionRate;
    private Long exitsInPeriod;
    private BigDecimal periodicalPayrollCost;
    private BigDecimal avgPayrollCost;
    private Integer payrollRunCount;
    private Long maleCount;
    private Long femaleCount;
    private BigDecimal malePercent;
    private BigDecimal femalePercent;
    private List<HeadcountTrendDTO> headcountTrend;
    private List<TopSellingItemDTO> topSellingItems;
    private List<UpcomingInvoiceDTO> upcomingReceivables;
    private List<UpcomingInvoiceDTO> upcomingPayables;
    private BudgetSummaryDTO budget;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HeadcountTrendDTO {
        private String label;
        private Long headcount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopSellingItemDTO {
        private String name;
        private String sku;
        private String category;
        private BigDecimal qtySold;
        private BigDecimal revenue;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpcomingInvoiceDTO {
        private Long id;
        private String invoiceNo;
        private String partyName;
        private BigDecimal outstanding;
        private Long daysUntilDue;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BudgetSummaryDTO {
        private boolean hasBudget;
        private BigDecimal incomeBudget;
        private BigDecimal expenseBudget;
        private BigDecimal incomeActual;
        private BigDecimal expenseActual;
        private BigDecimal incomeVariance;
        private BigDecimal expenseVariance;
        private BigDecimal netBudget;
        private BigDecimal netActual;
        private BigDecimal netVariance;
        private BigDecimal incomeVariancePct;
        private BigDecimal expenseVariancePct;
    }
}
