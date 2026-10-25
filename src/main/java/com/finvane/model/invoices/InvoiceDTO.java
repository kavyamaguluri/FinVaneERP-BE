package com.finvane.model.invoices;

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
public class InvoiceDTO {
    private Long id;
    private String invoiceNo;
    private String invoiceType;
    private Long partyId;
    private String partyName;
    private String partyGstin;
    private Long locationId;
    private String locationCode;
    private String invoiceDate;
    private String dueDate;
    private String placeOfSupply;
    private BigDecimal taxableValue;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal igst;
    private BigDecimal totalAmount;
    private String status;
    private Long voucherId;
    private String irn;
    private String irnAckNo;
    private String irnAckDate;
    private String irnQr;
    private String ewbNo;
    private List<InvoiceLineDTO> lines;
}
