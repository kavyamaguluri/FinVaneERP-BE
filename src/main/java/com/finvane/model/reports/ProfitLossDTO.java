package com.finvane.model.reports;

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
public class ProfitLossDTO {
    private String fromDate;
    private String toDate;
    private BigDecimal revenueFromOperations;
    private BigDecimal otherIncome;
    private BigDecimal totalRevenue;
    private BigDecimal costOfMaterials;
    private BigDecimal purchaseOfStock;
    private BigDecimal employeeBenefitExpense;
    private BigDecimal depreciationExpense;
    private BigDecimal otherExpenses;
    private BigDecimal totalExpenses;
    private BigDecimal profitBeforeTax;
    private BigDecimal taxExpense;
    private BigDecimal netProfit;
    private List<ReportLine> revenueLines;
    private List<ReportLine> expenseLines;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportLine {
        private String code;
        private String name;
        private BigDecimal amount;
    }
}
