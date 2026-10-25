package com.finvane.model.budgets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetEntryDTO {
    private Long id;
    private Long accountId;
    private String accountCode;
    private String accountName;
    private String accountType;
    private String periodStart;
    private String periodEnd;
    private BigDecimal amount;
    private BigDecimal actual;
    private BigDecimal variance;
    private BigDecimal variancePct;
    private String notes;
}
