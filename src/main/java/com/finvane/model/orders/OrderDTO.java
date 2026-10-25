package com.finvane.model.orders;

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
public class OrderDTO {
    private Long id;
    private String orderNo;
    private String orderType; // 'sales' or 'purchase'
    private Long partyId;
    private String partyName;
    private Long locationId;
    private String locationCode;
    private String orderDate;
    private String expectedDate;
    private BigDecimal taxableValue;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal igst;
    private BigDecimal totalAmount;
    private String status;
    private String notes;
    private List<OrderLineDTO> lines;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderLineDTO {
        private Long id;
        private Long orderId;
        private Long itemId;
        private String itemName;
        private BigDecimal qty;
        private BigDecimal rate;
        private BigDecimal taxableValue;
        private BigDecimal gstRate;
        private BigDecimal cgst;
        private BigDecimal sgst;
        private BigDecimal igst;
        private BigDecimal amount;
    }
}
