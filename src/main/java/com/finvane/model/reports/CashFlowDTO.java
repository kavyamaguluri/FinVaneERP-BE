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
public class CashFlowDTO {
    private String fromDate;
    private String toDate;
    private BigDecimal operatingCashFlow;
    private BigDecimal investingCashFlow;
    private BigDecimal financingCashFlow;
    private BigDecimal netCashFlow;
    private BigDecimal openingCash;
    private BigDecimal closingCash;
    private List<CashFlowItem> operatingItems;
    private List<CashFlowItem> investingItems;
    private List<CashFlowItem> financingItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CashFlowItem {
        private String description;
        private BigDecimal amount;
    }
}
