package com.finvane.model.compliance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Gstr3bDTO {
    private String period;
    private String gstin;
    // Table 3.1 Outward supplies
    private BigDecimal outwardTaxable;
    private BigDecimal outwardIgst;
    private BigDecimal outwardCgst;
    private BigDecimal outwardSgst;
    private BigDecimal outwardCess;
    // Table 4 Eligible ITC
    private BigDecimal itcIgst;
    private BigDecimal itcCgst;
    private BigDecimal itcSgst;
    private BigDecimal itcCess;
    // Net Tax Payable
    private BigDecimal netPayableIgst;
    private BigDecimal netPayableCgst;
    private BigDecimal netPayableSgst;
    private BigDecimal totalTaxPayable;
}
