package com.finvane.model.budgets;

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
public class BudgetPlanDTO {
    private int fy;
    private List<BudgetMonthHeader> months;
    private List<BudgetPlanRow> grid;
    private List<BigDecimal> monthTotals;
    private BigDecimal incomeTotal;
    private BigDecimal expenseTotal;
    private BigDecimal netPlanned;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BudgetMonthHeader {
        private String label;
        private String periodStart;
        private String periodEnd;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BudgetPlanRow {
        private Long id;
        private String code;
        private String name;
        private String type;
        private List<BudgetCell> cells;
        private BigDecimal rowTotal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BudgetCell {
        private String periodStart;
        private BigDecimal amount;
    }
}
