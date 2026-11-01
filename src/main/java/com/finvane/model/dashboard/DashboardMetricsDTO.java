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
    private List<LowStockItemDTO> lowStock;
    private List<StockByLocationDTO> stockByLocation;
    private TransferStatsDTO transferStats;
    private BudgetSummaryDTO budget;
    private CfoSummaryDTO cfoSummary;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StockByLocationDTO {
        private Long id;
        private String code;
        private String name;
        private String locationType;
        private BigDecimal value;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TransferStatsDTO {
        private Long cnt;
        private BigDecimal igst;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LowStockItemDTO {
        private Long id;
        private String name;
        private String sku;
        private BigDecimal currentStock;
        private BigDecimal reorderLevel;
    }

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

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CfoSummaryDTO {
        private String from;
        private String to;
        private List<String> narrative;
        private List<CfoFlagDTO> flags;
        private CfoKpisDTO kpis;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CfoFlagDTO {
        private String tone; // "danger", "warning", "success"
        private String text;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CfoKpisDTO {
        private BigDecimal salesTotal;
        private BigDecimal purchaseTotal;
        private BigDecimal grossProfit;
        private BigDecimal grossMarginPct;
        private BigDecimal netProfit;
        private BigDecimal netMarginPct;
        private BigDecimal cashBank;
        private BigDecimal debtors;
        private BigDecimal creditors;
        private BigDecimal currentRatio;
        private BigDecimal quickRatio;
        private BigDecimal dso;
        private BigDecimal dpo;
        private BigDecimal cashRunwayMonths;
    }
}

