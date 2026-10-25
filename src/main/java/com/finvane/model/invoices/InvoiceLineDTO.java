package com.finvane.model.invoices;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceLineDTO {
    private Long id;
    private Long invoiceId;
    private Long itemId;
    private String itemName;
    private String itemType;
    private String hsnSac;
    private BigDecimal qty;
    private BigDecimal rate;
    private BigDecimal taxableValue;
    private BigDecimal gstRate;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal igst;
    private BigDecimal amount;
    private String description;
}
