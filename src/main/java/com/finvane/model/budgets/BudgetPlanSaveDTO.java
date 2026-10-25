package com.finvane.model.budgets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetPlanSaveDTO {
    private int fy;
    // Map of accountId -> (Map of periodStart -> amount)
    private Map<String, Map<String, BigDecimal>> amounts;
}
