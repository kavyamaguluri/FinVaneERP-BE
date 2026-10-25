package com.finvane.model.production;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionOrderDTO {
    private Long id;
    private String orderNo;
    private Long bomId;
    private String bomCode;
    private Long outputItemId;
    private String outputItemName;
    private Long locationId;
    private String locationCode;
    private BigDecimal plannedQty;
    private BigDecimal producedQty;
    private String orderDate;
    private String status;
    private BigDecimal totalCost;
}
