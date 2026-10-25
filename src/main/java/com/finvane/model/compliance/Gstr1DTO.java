package com.finvane.model.compliance;

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
public class Gstr1DTO {
    private String period;
    private String gstin;
    private BigDecimal totalTaxable;
    private BigDecimal totalCgst;
    private BigDecimal totalSgst;
    private BigDecimal totalIgst;
    private BigDecimal totalTax;
    private List<Gstr1B2BInvoice> b2b;
    private List<Gstr1HsnSummary> hsn;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Gstr1B2BInvoice {
        private String customerGstin;
        private String customerName;
        private String invoiceNo;
        private String invoiceDate;
        private BigDecimal invoiceValue;
        private String pos;
        private BigDecimal taxableValue;
        private BigDecimal cess;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Gstr1HsnSummary {
        private String hsn;
        private String description;
        private String uqc;
        private BigDecimal totalQty;
        private BigDecimal totalValue;
        private BigDecimal taxableValue;
        private BigDecimal integratedTax;
        private BigDecimal centralTax;
        private BigDecimal stateTax;
    }
}
