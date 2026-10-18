package com.arcsetu.v2.model.accounts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class TrialBalanceRowDTO {

    private TrialBalanceRowDTO() {}

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Row {
        private Long ledgerId;
        private String ledgerCode;
        private String ledgerName;
        private String groupName;
        private String nature;
        private BigDecimal debitTotal;
        private BigDecimal creditTotal;
        private BigDecimal closingDebit;
        private BigDecimal closingCredit;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Report {
        private LocalDate asOfDate;
        private List<Row> rows;
        private BigDecimal totalDebit;
        private BigDecimal totalCredit;
        private boolean isBalanced;
    }
}
