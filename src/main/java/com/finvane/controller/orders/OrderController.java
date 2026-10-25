package com.finvane.controller.orders;

import com.finvane.dao.common.ILocationJpaAccess;
import com.finvane.dao.common.IPartyJpaAccess;
import com.finvane.dao.inventory.IItemJpaAccess;
import com.finvane.dao.orders.*;
import com.finvane.entity.common.LocationEntity;
import com.finvane.entity.common.PartyEntity;
import com.finvane.entity.inventory.ItemEntity;
import com.finvane.entity.orders.*;
import com.finvane.model.common.ApiResponse;
import com.finvane.model.common.TenantContext;
import com.finvane.model.orders.OrderDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/orders")
@RequiredArgsConstructor
public class OrderController {

    private final ISalesOrderJpaAccess salesOrderDao;
    private final ISalesOrderLineJpaAccess salesOrderLineDao;
    private final IPurchaseOrderJpaAccess purchaseOrderDao;
    private final IPurchaseOrderLineJpaAccess purchaseOrderLineDao;
    private final IPartyJpaAccess partyDao;
    private final ILocationJpaAccess locationDao;
    private final IItemJpaAccess itemDao;

    @GetMapping
    public ApiResponse<List<OrderDTO>> getOrders(@RequestParam(required = false, defaultValue = "sales") String type) {
        String tenantId = TenantContext.getTenantId();
        Map<Long, PartyEntity> partyMap = partyDao.findByTenantIdAndIsActiveTrueOrderByNameAsc(tenantId).stream()
                .collect(Collectors.toMap(PartyEntity::getId, p -> p, (a, b) -> a));
        Map<Long, LocationEntity> locMap = locationDao.findByTenantIdAndIsActiveTrueOrderByNameAsc(tenantId).stream()
                .collect(Collectors.toMap(LocationEntity::getId, l -> l, (a, b) -> a));

        if ("purchase".equalsIgnoreCase(type)) {
            List<PurchaseOrderEntity> pos = purchaseOrderDao.findByTenantIdOrderByIdDesc(tenantId);
            List<OrderDTO> dtos = pos.stream().map(po -> {
                PartyEntity p = partyMap.get(po.getVendorId());
                LocationEntity l = locMap.get(po.getLocationId());
                return OrderDTO.builder()
                        .id(po.getId())
                        .orderNo(po.getPoNo())
                        .orderType("purchase")
                        .partyId(po.getVendorId())
                        .partyName(p != null ? p.getName() : "Vendor #" + po.getVendorId())
                        .locationId(po.getLocationId())
                        .locationCode(l != null ? l.getCode() : "")
                        .orderDate(po.getPoDate())
                        .expectedDate(po.getExpectedDate())
                        .taxableValue(po.getTaxableValue())
                        .cgst(po.getCgst())
                        .sgst(po.getSgst())
                        .igst(po.getIgst())
                        .totalAmount(po.getTotalAmount())
                        .status(po.getStatus())
                        .build();
            }).toList();
            return ApiResponse.ok(dtos);
        } else {
            List<SalesOrderEntity> sos = salesOrderDao.findByTenantIdOrderByIdDesc(tenantId);
            List<OrderDTO> dtos = sos.stream().map(so -> {
                PartyEntity p = partyMap.get(so.getCustomerId());
                LocationEntity l = locMap.get(so.getLocationId());
                return OrderDTO.builder()
                        .id(so.getId())
                        .orderNo(so.getSoNo())
                        .orderType("sales")
                        .partyId(so.getCustomerId())
                        .partyName(p != null ? p.getName() : "Customer #" + so.getCustomerId())
                        .locationId(so.getLocationId())
                        .locationCode(l != null ? l.getCode() : "")
                        .orderDate(so.getSoDate())
                        .expectedDate(so.getExpectedDate())
                        .taxableValue(so.getTaxableValue())
                        .cgst(so.getCgst())
                        .sgst(so.getSgst())
                        .igst(so.getIgst())
                        .totalAmount(so.getTotalAmount())
                        .status(so.getStatus())
                        .notes(so.getNotes())
                        .build();
            }).toList();
            return ApiResponse.ok(dtos);
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDTO> getOrderById(@PathVariable Long id, @RequestParam(required = false, defaultValue = "sales") String type) {
        String tenantId = TenantContext.getTenantId();
        Map<Long, ItemEntity> itemMap = itemDao.findByTenantIdOrderByNameAsc(tenantId).stream()
                .collect(Collectors.toMap(ItemEntity::getId, it -> it, (a, b) -> a));

        if ("purchase".equalsIgnoreCase(type)) {
            PurchaseOrderEntity po = purchaseOrderDao.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Purchase Order not found with ID: " + id));
            PartyEntity p = partyDao.findById(po.getVendorId()).orElse(null);
            LocationEntity l = po.getLocationId() != null ? locationDao.findById(po.getLocationId()).orElse(null) : null;
            List<PurchaseOrderLineEntity> lines = purchaseOrderLineDao.findByTenantIdAndPoId(tenantId, id);

            List<OrderDTO.OrderLineDTO> lineDTOs = lines.stream().map(line -> {
                ItemEntity item = itemMap.get(line.getItemId());
                return OrderDTO.OrderLineDTO.builder()
                        .id(line.getId())
                        .orderId(line.getPoId())
                        .itemId(line.getItemId())
                        .itemName(item != null ? item.getName() : "Item #" + line.getItemId())
                        .qty(line.getQty())
                        .rate(line.getRate())
                        .taxableValue(line.getTaxableValue())
                        .gstRate(line.getGstRate())
                        .cgst(line.getCgst())
                        .sgst(line.getSgst())
                        .igst(line.getIgst())
                        .amount(line.getAmount())
                        .build();
            }).toList();

            return ApiResponse.ok(OrderDTO.builder()
                    .id(po.getId())
                    .orderNo(po.getPoNo())
                    .orderType("purchase")
                    .partyId(po.getVendorId())
                    .partyName(p != null ? p.getName() : "")
                    .locationId(po.getLocationId())
                    .locationCode(l != null ? l.getCode() : "")
                    .orderDate(po.getPoDate())
                    .expectedDate(po.getExpectedDate())
                    .taxableValue(po.getTaxableValue())
                    .cgst(po.getCgst())
                    .sgst(po.getSgst())
                    .igst(po.getIgst())
                    .totalAmount(po.getTotalAmount())
                    .status(po.getStatus())
                    .lines(lineDTOs)
                    .build());
        } else {
            SalesOrderEntity so = salesOrderDao.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Sales Order not found with ID: " + id));
            PartyEntity p = partyDao.findById(so.getCustomerId()).orElse(null);
            LocationEntity l = so.getLocationId() != null ? locationDao.findById(so.getLocationId()).orElse(null) : null;
            List<SalesOrderLineEntity> lines = salesOrderLineDao.findByTenantIdAndSalesOrderId(tenantId, id);

            List<OrderDTO.OrderLineDTO> lineDTOs = lines.stream().map(line -> {
                ItemEntity item = itemMap.get(line.getItemId());
                return OrderDTO.OrderLineDTO.builder()
                        .id(line.getId())
                        .orderId(line.getSalesOrderId())
                        .itemId(line.getItemId())
                        .itemName(item != null ? item.getName() : "Item #" + line.getItemId())
                        .qty(line.getQty())
                        .rate(line.getRate())
                        .taxableValue(line.getTaxableValue())
                        .gstRate(line.getGstRate())
                        .cgst(line.getCgst())
                        .sgst(line.getSgst())
                        .igst(line.getIgst())
                        .amount(line.getAmount())
                        .build();
            }).toList();

            return ApiResponse.ok(OrderDTO.builder()
                    .id(so.getId())
                    .orderNo(so.getSoNo())
                    .orderType("sales")
                    .partyId(so.getCustomerId())
                    .partyName(p != null ? p.getName() : "")
                    .locationId(so.getLocationId())
                    .locationCode(l != null ? l.getCode() : "")
                    .orderDate(so.getSoDate())
                    .expectedDate(so.getExpectedDate())
                    .taxableValue(so.getTaxableValue())
                    .cgst(so.getCgst())
                    .sgst(so.getSgst())
                    .igst(so.getIgst())
                    .totalAmount(so.getTotalAmount())
                    .status(so.getStatus())
                    .notes(so.getNotes())
                    .lines(lineDTOs)
                    .build());
        }
    }

    @PostMapping
    @Transactional
    public ApiResponse<OrderDTO> createOrder(@RequestBody OrderDTO dto) {
        String tenantId = TenantContext.getTenantId();
        String type = (dto.getOrderType() != null && dto.getOrderType().equalsIgnoreCase("purchase")) ? "purchase" : "sales";

        PartyEntity party = partyDao.findById(dto.getPartyId())
                .orElseThrow(() -> new IllegalArgumentException("Party not found with ID: " + dto.getPartyId()));

        BigDecimal totalTaxable = BigDecimal.ZERO;
        BigDecimal totalCgst = BigDecimal.ZERO;
        BigDecimal totalSgst = BigDecimal.ZERO;
        BigDecimal totalIgst = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        List<OrderDTO.OrderLineDTO> inputLines = dto.getLines() != null ? dto.getLines() : Collections.emptyList();
        List<SalesOrderLineEntity> soLines = new ArrayList<>();
        List<PurchaseOrderLineEntity> poLines = new ArrayList<>();

        for (OrderDTO.OrderLineDTO l : inputLines) {
            if (l.getItemId() == null || l.getQty() == null || l.getQty().compareTo(BigDecimal.ZERO) <= 0) continue;
            ItemEntity item = itemDao.findById(l.getItemId()).orElse(null);
            BigDecimal rate = l.getRate() != null ? l.getRate() : BigDecimal.ZERO;
            BigDecimal taxable = l.getQty().multiply(rate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal gstRate = l.getGstRate() != null ? l.getGstRate() : BigDecimal.valueOf(18);

            BigDecimal cgst = BigDecimal.ZERO;
            BigDecimal sgst = BigDecimal.ZERO;
            BigDecimal igst = BigDecimal.ZERO;

            boolean isIntra = party.getStateCode() == null || "27".equals(party.getStateCode());
            if (isIntra) {
                BigDecimal halfRate = gstRate.divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
                cgst = taxable.multiply(halfRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                sgst = taxable.multiply(halfRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            } else {
                igst = taxable.multiply(gstRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
            BigDecimal lineTotal = taxable.add(cgst).add(sgst).add(igst);

            totalTaxable = totalTaxable.add(taxable);
            totalCgst = totalCgst.add(cgst);
            totalSgst = totalSgst.add(sgst);
            totalIgst = totalIgst.add(igst);
            totalAmount = totalAmount.add(lineTotal);

            if ("purchase".equals(type)) {
                poLines.add(PurchaseOrderLineEntity.builder()
                        .tenantId(tenantId)
                        .itemId(l.getItemId())
                        .qty(l.getQty())
                        .rate(rate)
                        .taxableValue(taxable)
                        .gstRate(gstRate)
                        .cgst(cgst)
                        .sgst(sgst)
                        .igst(igst)
                        .amount(lineTotal)
                        .build());
            } else {
                soLines.add(SalesOrderLineEntity.builder()
                        .tenantId(tenantId)
                        .itemId(l.getItemId())
                        .qty(l.getQty())
                        .rate(rate)
                        .taxableValue(taxable)
                        .gstRate(gstRate)
                        .cgst(cgst)
                        .sgst(sgst)
                        .igst(igst)
                        .amount(lineTotal)
                        .build());
            }
        }

        if ("purchase".equals(type)) {
            long count = purchaseOrderDao.countByTenantId(tenantId);
            String poNo = String.format("PO-%05d", count + 1);

            PurchaseOrderEntity po = PurchaseOrderEntity.builder()
                    .tenantId(tenantId)
                    .poNo(poNo)
                    .vendorId(party.getId())
                    .locationId(dto.getLocationId())
                    .poDate(dto.getOrderDate() != null ? dto.getOrderDate() : LocalDate.now().toString())
                    .expectedDate(dto.getExpectedDate())
                    .taxableValue(totalTaxable)
                    .cgst(totalCgst)
                    .sgst(totalSgst)
                    .igst(totalIgst)
                    .totalAmount(totalAmount)
                    .status("open")
                    .build();
            PurchaseOrderEntity saved = purchaseOrderDao.save(po);

            for (PurchaseOrderLineEntity line : poLines) {
                line.setPoId(saved.getId());
            }
            purchaseOrderLineDao.saveAll(poLines);

            dto.setId(saved.getId());
            dto.setOrderNo(poNo);
            dto.setOrderType("purchase");
        } else {
            long count = salesOrderDao.countByTenantId(tenantId);
            String soNo = String.format("SO-%05d", count + 1);

            SalesOrderEntity so = SalesOrderEntity.builder()
                    .tenantId(tenantId)
                    .soNo(soNo)
                    .customerId(party.getId())
                    .locationId(dto.getLocationId())
                    .soDate(dto.getOrderDate() != null ? dto.getOrderDate() : LocalDate.now().toString())
                    .expectedDate(dto.getExpectedDate())
                    .taxableValue(totalTaxable)
                    .cgst(totalCgst)
                    .sgst(totalSgst)
                    .igst(totalIgst)
                    .totalAmount(totalAmount)
                    .status("open")
                    .notes(dto.getNotes())
                    .build();
            SalesOrderEntity saved = salesOrderDao.save(so);

            for (SalesOrderLineEntity line : soLines) {
                line.setSalesOrderId(saved.getId());
            }
            salesOrderLineDao.saveAll(soLines);

            dto.setId(saved.getId());
            dto.setOrderNo(soNo);
            dto.setOrderType("sales");
        }

        dto.setTaxableValue(totalTaxable);
        dto.setCgst(totalCgst);
        dto.setSgst(totalSgst);
        dto.setIgst(totalIgst);
        dto.setTotalAmount(totalAmount);
        dto.setStatus("open");

        return ApiResponse.ok("Order created successfully", dto);
    }
}
