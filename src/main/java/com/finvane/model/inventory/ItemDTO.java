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
public class ItemDTO {
    private Long id;
    private String sku;
    private String name;
    private String itemType;
    private String category;
    private String brand;
    private String hsnSac;
    private String uom;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private BigDecimal gstRate;
    private BigDecimal openingStock;
    private BigDecimal reorderLevel;
    private String itemClass;
    private Long isActive;
}
