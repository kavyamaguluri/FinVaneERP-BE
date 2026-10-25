package com.finvane.model.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectStockDTO {
    private Long id;
    private String entryDate;
    private Long locationId;
    private String locationCode;
    private BigDecimal valuationAmount;
    private String notes;
}
