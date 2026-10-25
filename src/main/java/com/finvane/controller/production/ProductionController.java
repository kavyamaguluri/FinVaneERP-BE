package com.finvane.controller.production;

import com.finvane.dao.common.ILocationJpaAccess;
import com.finvane.dao.inventory.IItemJpaAccess;
import com.finvane.dao.production.IBomJpaAccess;
import com.finvane.dao.production.IBomLineJpaAccess;
import com.finvane.dao.production.IProductionOrderJpaAccess;
import com.finvane.entity.common.LocationEntity;
import com.finvane.entity.inventory.ItemEntity;
import com.finvane.entity.production.BomEntity;
import com.finvane.entity.production.BomLineEntity;
import com.finvane.entity.production.ProductionOrderEntity;
import com.finvane.model.common.ApiResponse;
import com.finvane.model.common.TenantContext;
import com.finvane.model.production.BomDTO;
import com.finvane.model.production.ProductionOrderDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/production")
@RequiredArgsConstructor
public class ProductionController {

    private final IBomJpaAccess bomDao;
    private final IBomLineJpaAccess bomLineDao;
    private final IProductionOrderJpaAccess orderDao;
    private final IItemJpaAccess itemDao;
    private final ILocationJpaAccess locationDao;

    // --- BOMS ---

    @GetMapping("/boms")
    public ApiResponse<List<BomDTO>> getBoms() {
        String tenantId = TenantContext.getTenantId();
        List<BomEntity> boms = bomDao.findByTenantIdAndIsActiveOrderByNameAsc(tenantId, 1L);
        Map<Long, ItemEntity> itemMap = itemDao.findByTenantIdOrderByNameAsc(tenantId).stream()
                .collect(Collectors.toMap(ItemEntity::getId, it -> it, (a, b) -> a));

        List<BomDTO> dtos = boms.stream().map(b -> {
            ItemEntity item = itemMap.get(b.getOutputItemId());
            List<BomLineEntity> lines = bomLineDao.findByTenantIdAndBomId(tenantId, b.getId());
            List<BomDTO.BomLineDTO> lineDTOs = lines.stream().map(l -> {
                ItemEntity comp = itemMap.get(l.getItemId());
                return BomDTO.BomLineDTO.builder()
                        .id(l.getId())
                        .bomId(l.getBomId())
                        .itemId(l.getItemId())
                        .itemName(comp != null ? comp.getName() : "Item #" + l.getItemId())
                        .qty(l.getQty())
                        .unitCost(l.getUnitCost())
                        .build();
            }).toList();

            return BomDTO.builder()
                    .id(b.getId())
                    .bomCode(b.getBomCode())
                    .name(b.getName())
                    .outputItemId(b.getOutputItemId())
                    .outputItemName(item != null ? item.getName() : "Item #" + b.getOutputItemId())
                    .batchSize(b.getBatchSize())
                    .labourCost(b.getLabourCost())
                    .overheadMode(b.getOverheadMode())
                    .overheadRate(b.getOverheadRate())
                    .isActive(b.getIsActive())
                    .lines(lineDTOs)
                    .build();
        }).toList();

        return ApiResponse.ok(dtos);
    }

    @PostMapping("/boms")
    @Transactional
    public ApiResponse<BomDTO> createBom(@RequestBody BomDTO dto) {
        String tenantId = TenantContext.getTenantId();
        long count = bomDao.countByTenantIdAndIsActive(tenantId, 1L);
        String bomCode = dto.getBomCode() != null && !dto.getBomCode().isBlank() ? dto.getBomCode() : String.format("BOM-%04d", count + 1);

        BomEntity entity = BomEntity.builder()
                .tenantId(tenantId)
                .bomCode(bomCode)
                .name(dto.getName())
                .outputItemId(dto.getOutputItemId())
                .batchSize(dto.getBatchSize() != null ? dto.getBatchSize() : BigDecimal.ONE)
                .labourCost(dto.getLabourCost() != null ? dto.getLabourCost() : BigDecimal.ZERO)
                .overheadMode(dto.getOverheadMode() != null ? dto.getOverheadMode() : "per_batch")
                .overheadRate(dto.getOverheadRate() != null ? dto.getOverheadRate() : BigDecimal.ZERO)
                .isActive(1L)
                .build();
        BomEntity saved = bomDao.save(entity);

        if (dto.getLines() != null) {
            List<BomLineEntity> lines = dto.getLines().stream().map(l -> BomLineEntity.builder()
                    .tenantId(tenantId)
                    .bomId(saved.getId())
                    .itemId(l.getItemId())
                    .qty(l.getQty())
                    .unitCost(l.getUnitCost() != null ? l.getUnitCost() : BigDecimal.ZERO)
                    .build()).toList();
            bomLineDao.saveAll(lines);
        }

        dto.setId(saved.getId());
        dto.setBomCode(bomCode);
        return ApiResponse.ok("BOM created successfully", dto);
    }

    // --- PRODUCTION ORDERS ---

    @GetMapping("/orders")
    public ApiResponse<List<ProductionOrderDTO>> getOrders() {
        String tenantId = TenantContext.getTenantId();
        List<ProductionOrderEntity> list = orderDao.findByTenantIdOrderByIdDesc(tenantId);
        Map<Long, ItemEntity> itemMap = itemDao.findByTenantIdOrderByNameAsc(tenantId).stream()
                .collect(Collectors.toMap(ItemEntity::getId, it -> it, (a, b) -> a));
        Map<Long, BomEntity> bomMap = bomDao.findAll().stream()
                .collect(Collectors.toMap(BomEntity::getId, b -> b, (a, b) -> a));
        Map<Long, LocationEntity> locMap = locationDao.findByTenantIdAndIsActiveTrueOrderByNameAsc(tenantId).stream()
                .collect(Collectors.toMap(LocationEntity::getId, l -> l, (a, b) -> a));

        List<ProductionOrderDTO> dtos = list.stream().map(o -> {
            ItemEntity item = itemMap.get(o.getOutputItemId());
            BomEntity bom = bomMap.get(o.getBomId());
            LocationEntity loc = locMap.get(o.getLocationId());
            return ProductionOrderDTO.builder()
                    .id(o.getId())
                    .orderNo(o.getOrderNo())
                    .bomId(o.getBomId())
                    .bomCode(bom != null ? bom.getBomCode() : "")
                    .outputItemId(o.getOutputItemId())
                    .outputItemName(item != null ? item.getName() : "Item #" + o.getOutputItemId())
                    .locationId(o.getLocationId())
                    .locationCode(loc != null ? loc.getCode() : "")
                    .plannedQty(o.getPlannedQty())
                    .producedQty(o.getProducedQty())
                    .orderDate(o.getOrderDate())
                    .status(o.getStatus())
                    .totalCost(o.getTotalCost())
                    .build();
        }).toList();

        return ApiResponse.ok(dtos);
    }

    @PostMapping("/orders")
    public ApiResponse<ProductionOrderDTO> createOrder(@RequestBody ProductionOrderDTO dto) {
        String tenantId = TenantContext.getTenantId();
        long count = orderDao.countByTenantId(tenantId);
        String orderNo = String.format("MO-%05d", count + 1);

        ProductionOrderEntity entity = ProductionOrderEntity.builder()
                .tenantId(tenantId)
                .orderNo(orderNo)
                .bomId(dto.getBomId())
                .outputItemId(dto.getOutputItemId())
                .locationId(dto.getLocationId())
                .plannedQty(dto.getPlannedQty())
                .producedQty(BigDecimal.ZERO)
                .orderDate(dto.getOrderDate() != null ? dto.getOrderDate() : LocalDate.now().toString())
                .status("planned")
                .totalCost(dto.getTotalCost() != null ? dto.getTotalCost() : BigDecimal.ZERO)
                .build();
        ProductionOrderEntity saved = orderDao.save(entity);
        dto.setId(saved.getId());
        dto.setOrderNo(orderNo);
        dto.setStatus("planned");
        return ApiResponse.ok("Production order created", dto);
    }

    @PostMapping("/orders/{id}/complete")
    public ApiResponse<Void> completeOrder(@PathVariable Long id) {
        ProductionOrderEntity entity = orderDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Production order not found with ID: " + id));
        entity.setStatus("completed");
        entity.setProducedQty(entity.getPlannedQty());
        orderDao.save(entity);
        return ApiResponse.ok("Production batch marked as completed", null);
    }
}
