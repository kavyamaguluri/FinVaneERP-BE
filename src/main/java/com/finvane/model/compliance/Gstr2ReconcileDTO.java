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
public class Gstr2ReconcileDTO {
    private String period;
    private long totalInvoices;
    private long matchedCount;
    private long mismatchedCount;
    private long missingInBooksCount;
    private long missingIn2bCount;
    private BigDecimal matchedItc;
    private BigDecimal atRiskItc;
    private List<ReconcileRecord> records;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReconcileRecord {
        private String vendorGstin;
        private String vendorName;
        private String invoiceNo;
        private String invoiceDate;
        private BigDecimal booksAmount;
        private BigDecimal portalAmount;
        private BigDecimal diffAmount;
        private String status; // 'MATCHED', 'MISMATCH', 'MISSING_IN_BOOKS', 'MISSING_IN_2B'
    }
}
