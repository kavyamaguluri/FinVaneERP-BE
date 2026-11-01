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
public class TrialBalanceDTO {
    private String fromDate;
    private String toDate;
    private String granularity;
    private Boolean dateRangeCorrected;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private boolean isBalanced;
    private List<TrialBalanceRow> rows;
    private TrialBalanceTotals totals;
    private List<TrialBalanceGroup> groups;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrialBalanceTotals {
        private BigDecimal openingDr;
        private BigDecimal openingCr;
        private BigDecimal debit;
        private BigDecimal credit;
        private BigDecimal closingDr;
        private BigDecimal closingCr;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrialBalanceGroup {
        private String groupName;
        private String nature;
        private BigDecimal totalDebit;
        private BigDecimal totalCredit;
        private List<TrialBalanceRow> accounts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrialBalanceRow {
        private Long id;
        private String code;
        private String name;
        private String primaryGroupName;
        private String secondaryGroupName;
        private String nature;
        private String type;
        private BigDecimal openingDr;
        private BigDecimal openingCr;
        private BigDecimal debit;
        private BigDecimal credit;
        private BigDecimal closingDr;
        private BigDecimal closingCr;
        private BigDecimal netBalance;
    }
}

