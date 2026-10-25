package com.finvane.model.orders;

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
public class QuotationDTO {
    private Long id;
    private String quoteNo;
    private Long partyId;
    private String partyName;
    private String partyGstin;
    private String quoteDate;
    private String validUntil;
    private BigDecimal taxableValue;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal igst;
    private BigDecimal totalAmount;
    private String status;
    private String terms;
    private List<QuotationLineDTO> lines;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuotationLineDTO {
        private Long id;
        private Long quotationId;
        private Long itemId;
        private String itemName;
        private BigDecimal qty;
        private BigDecimal rate;
        private BigDecimal taxableValue;
        private BigDecimal gstRate;
        private BigDecimal cgst;
        private BigDecimal sgst;
        private BigDecimal igst;
        private BigDecimal amount;
    }
}
