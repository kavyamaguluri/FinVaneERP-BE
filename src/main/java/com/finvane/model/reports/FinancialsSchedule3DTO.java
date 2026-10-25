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
public class FinancialsSchedule3DTO {
    private String entityName;
    private String cin;
    private String pan;
    private String asOfDate;
    private BalanceSheetDTO balanceSheet;
    private ProfitLossDTO profitLoss;
    private CashFlowDTO cashFlow;
    private List<Schedule3Note> notes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Schedule3Note {
        private String noteNumber;
        private String title;
        private BigDecimal noteTotal;
        private List<NoteLine> lines;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoteLine {
        private String particulars;
        private BigDecimal currentPeriod;
        private BigDecimal previousPeriod;
    }
}
