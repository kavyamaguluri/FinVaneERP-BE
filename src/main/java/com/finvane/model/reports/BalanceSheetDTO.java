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
public class BalanceSheetDTO {
    private String asOfDate;
    private BigDecimal totalLiabilitiesAndEquity;
    private BigDecimal totalAssets;
    private boolean isBalanced;
    private List<BalanceSheetSection> equityAndLiabilities;
    private List<BalanceSheetSection> assets;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BalanceSheetSection {
        private String title;
        private BigDecimal total;
        private List<BalanceSheetLine> lines;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BalanceSheetLine {
        private String code;
        private String name;
        private BigDecimal amount;
    }
}
