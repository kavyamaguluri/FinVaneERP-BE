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
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private boolean isBalanced;
    private List<TrialBalanceGroup> groups;

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
        private BigDecimal debit;
        private BigDecimal credit;
        private BigDecimal netBalance;
    }
}
