package com.finvane.controller.inventory;

import com.finvane.dao.common.ILocationJpaAccess;
import com.finvane.dao.inventory.IDirectStockJpaAccess;
import com.finvane.dao.inventory.IItemJpaAccess;
import com.finvane.entity.common.LocationEntity;
import com.finvane.entity.inventory.DirectStockEntity;
import com.finvane.entity.inventory.ItemEntity;
import com.finvane.model.common.ApiResponse;
import com.finvane.model.common.TenantContext;
import com.finvane.model.inventory.DirectStockDTO;
import com.finvane.model.inventory.ItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final IItemJpaAccess itemDao;
    private final IDirectStockJpaAccess directStockDao;
    private final ILocationJpaAccess locationDao;

    // --- ITEMS ---

    @GetMapping("/items")
    public ApiResponse<List<ItemDTO>> getItems(@RequestParam(required = false) String itemType) {
        String tenantId = TenantContext.getTenantId();
        List<ItemEntity> list;
        if (itemType != null && !itemType.isBlank()) {
            list = itemDao.findByTenantIdAndItemTypeAndIsActiveOrderByNameAsc(tenantId, itemType.toLowerCase(), 1L);
        } else {
            list = itemDao.findByTenantIdAndIsActiveOrderByNameAsc(tenantId, 1L);
        }
        return ApiResponse.ok(list.stream().map(this::toItemDTO).toList());
    }

    @PostMapping("/items")
    public ApiResponse<ItemDTO> createItem(@RequestBody ItemDTO dto) {
        String tenantId = TenantContext.getTenantId();
        ItemEntity entity = ItemEntity.builder()
                .tenantId(tenantId)
                .sku(dto.getSku())
                .name(dto.getName())
                .itemType(dto.getItemType() != null ? dto.getItemType() : "goods")
                .category(dto.getCategory())
                .brand(dto.getBrand())
                .hsnSac(dto.getHsnSac())
                .uom(dto.getUom())
                .purchasePrice(dto.getPurchasePrice())
                .salePrice(dto.getSalePrice())
                .gstRate(dto.getGstRate())
                .openingStock(dto.getOpeningStock())
                .reorderLevel(dto.getReorderLevel())
                .itemClass(dto.getItemClass())
                .isActive(1L)
                .build();
        ItemEntity saved = itemDao.save(entity);
        return ApiResponse.ok("Item saved successfully", toItemDTO(saved));
    }

    @GetMapping("/items/{id}")
    public ApiResponse<ItemDTO> getItemById(@PathVariable Long id) {
        ItemEntity entity = itemDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found with ID: " + id));
        return ApiResponse.ok(toItemDTO(entity));
    }

    @PutMapping("/items/{id}")
    public ApiResponse<ItemDTO> updateItem(@PathVariable Long id, @RequestBody ItemDTO dto) {
        ItemEntity entity = itemDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found with ID: " + id));

        entity.setSku(dto.getSku());
        entity.setName(dto.getName());
        if (dto.getItemType() != null) entity.setItemType(dto.getItemType());
        entity.setCategory(dto.getCategory());
        entity.setBrand(dto.getBrand());
        entity.setHsnSac(dto.getHsnSac());
        entity.setUom(dto.getUom());
        entity.setPurchasePrice(dto.getPurchasePrice());
        entity.setSalePrice(dto.getSalePrice());
        entity.setGstRate(dto.getGstRate());
        entity.setOpeningStock(dto.getOpeningStock());
        entity.setReorderLevel(dto.getReorderLevel());
        entity.setItemClass(dto.getItemClass());
        if (dto.getIsActive() != null) entity.setIsActive(dto.getIsActive());

        ItemEntity saved = itemDao.save(entity);
        return ApiResponse.ok("Item updated successfully", toItemDTO(saved));
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<Void> deleteItem(@PathVariable Long id) {
        itemDao.deleteById(id);
        return ApiResponse.ok("Item deleted successfully", null);
    }

    // --- DIRECT STOCK VALUATION ---

    @GetMapping("/direct-stock")
    public ApiResponse<List<DirectStockDTO>> getDirectStockEntries() {
        String tenantId = TenantContext.getTenantId();
        List<DirectStockEntity> list = directStockDao.findByTenantIdOrderByEntryDateDesc(tenantId);
        Map<Long, LocationEntity> locMap = locationDao.findByTenantIdAndIsActiveTrueOrderByNameAsc(tenantId).stream()
                .collect(Collectors.toMap(LocationEntity::getId, l -> l, (a, b) -> a));

        List<DirectStockDTO> dtos = list.stream().map(e -> {
            LocationEntity l = locMap.get(e.getLocationId());
            return DirectStockDTO.builder()
                    .id(e.getId())
                    .entryDate(e.getEntryDate())
                    .locationId(e.getLocationId())
                    .locationCode(l != null ? l.getCode() : "")
                    .valuationAmount(e.getValuationAmount())
                    .notes(e.getNotes())
                    .build();
        }).toList();

        return ApiResponse.ok(dtos);
    }

    @PostMapping("/direct-stock")
    public ApiResponse<DirectStockDTO> createDirectStockEntry(@RequestBody DirectStockDTO dto) {
        String tenantId = TenantContext.getTenantId();
        DirectStockEntity entity = DirectStockEntity.builder()
                .tenantId(tenantId)
                .entryDate(dto.getEntryDate() != null ? dto.getEntryDate() : LocalDate.now().toString())
                .locationId(dto.getLocationId())
                .valuationAmount(dto.getValuationAmount())
                .notes(dto.getNotes())
                .build();
        DirectStockEntity saved = directStockDao.save(entity);
        dto.setId(saved.getId());
        return ApiResponse.ok("Direct stock valuation recorded", dto);
    }

    private ItemDTO toItemDTO(ItemEntity e) {
        return ItemDTO.builder()
                .id(e.getId())
                .sku(e.getSku())
                .name(e.getName())
                .itemType(e.getItemType())
                .category(e.getCategory())
                .brand(e.getBrand())
                .hsnSac(e.getHsnSac())
                .uom(e.getUom())
                .purchasePrice(e.getPurchasePrice())
                .salePrice(e.getSalePrice())
                .gstRate(e.getGstRate())
                .openingStock(e.getOpeningStock())
                .reorderLevel(e.getReorderLevel())
                .itemClass(e.getItemClass())
                .isActive(e.getIsActive())
                .build();
    }
}
