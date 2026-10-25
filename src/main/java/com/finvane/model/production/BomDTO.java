package com.finvane.model.production;

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
public class BomDTO {
    private Long id;
    private String bomCode;
    private String name;
    private Long outputItemId;
    private String outputItemName;
    private BigDecimal batchSize;
    private BigDecimal labourCost;
    private String overheadMode;
    private BigDecimal overheadRate;
    private Long isActive;
    private List<BomLineDTO> lines;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BomLineDTO {
        private Long id;
        private Long bomId;
        private Long itemId;
        private String itemName;
        private BigDecimal qty;
        private BigDecimal unitCost;
    }
}
