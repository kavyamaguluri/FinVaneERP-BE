package com.finvane.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public final class FifoValuationUtility {

    private FifoValuationUtility() {}

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FifoLot {
        private Long lotId;
        private BigDecimal remainingQty;
        private BigDecimal unitCost;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsumptionItem {
        private Long lotId;
        private BigDecimal consumedQty;
        private BigDecimal unitCost;
        private BigDecimal totalCost;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FifoResult {
        private BigDecimal totalCogs;
        private BigDecimal totalConsumedQty;
        private List<ConsumptionItem> consumptions;
        private boolean isFullySatisfied;
    }

    /**
     * Consumes lots in strict FIFO order until requiredQty is fulfilled.
     */
    public static FifoResult consumeFifo(List<FifoLot> lots, BigDecimal requiredQty) {
        if (requiredQty == null || requiredQty.compareTo(BigDecimal.ZERO) <= 0) {
            return FifoResult.builder()
                    .totalCogs(BigDecimal.ZERO)
                    .totalConsumedQty(BigDecimal.ZERO)
                    .consumptions(new ArrayList<>())
                    .isFullySatisfied(true)
                    .build();
        }

        BigDecimal needed = requiredQty;
        BigDecimal totalCogs = BigDecimal.ZERO;
        BigDecimal totalConsumed = BigDecimal.ZERO;
        List<ConsumptionItem> items = new ArrayList<>();

        for (FifoLot lot : lots) {
            if (needed.compareTo(BigDecimal.ZERO) <= 0) break;
            if (lot.getRemainingQty().compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal take = lot.getRemainingQty().min(needed);
            BigDecimal cost = take.multiply(lot.getUnitCost()).setScale(2, RoundingMode.HALF_UP);

            lot.setRemainingQty(lot.getRemainingQty().subtract(take));
            needed = needed.subtract(take);
            totalConsumed = totalConsumed.add(take);
            totalCogs = totalCogs.add(cost);

            items.add(ConsumptionItem.builder()
                    .lotId(lot.getLotId())
                    .consumedQty(take)
                    .unitCost(lot.getUnitCost())
                    .totalCost(cost)
                    .build());
        }

        return FifoResult.builder()
                .totalCogs(totalCogs)
                .totalConsumedQty(totalConsumed)
                .consumptions(items)
                .isFullySatisfied(needed.compareTo(BigDecimal.ZERO) == 0)
                .build();
    }
}
