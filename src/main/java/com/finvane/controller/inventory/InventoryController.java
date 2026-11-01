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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
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
    private final JdbcTemplate jdbcTemplate;

    // --- ITEMS ---

    @GetMapping("/items")
    public ApiResponse<List<ItemDTO>> getItems(@RequestParam(required = false) String itemType) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";

        String stockSql = "SELECT item_id, SUM(balance_qty) as total FROM (" +
                "  SELECT item_id, location_id, balance_qty, ROW_NUMBER() OVER (PARTITION BY location_id, item_id ORDER BY id DESC) rn " +
                "  FROM stock_ledger WHERE tenant_id = ?" +
                ") latest WHERE latest.rn = 1 GROUP BY item_id";

        Map<Long, BigDecimal> stockMap = new HashMap<>();
        try {
            List<Map<String, Object>> stockRows = jdbcTemplate.queryForList(stockSql, tenantId);
            for (Map<String, Object> r : stockRows) {
                Number iid = (Number) r.get("item_id");
                Number tot = (Number) r.get("total");
                if (iid != null && tot != null) {
                    stockMap.put(iid.longValue(), new BigDecimal(tot.toString()));
                }
            }
        } catch (Exception ignored) {}

        List<ItemEntity> list;
        if (itemType != null && !itemType.isBlank()) {
            list = itemDao.findByTenantIdAndItemTypeAndIsActiveOrderByNameAsc(tenantId, itemType.toLowerCase(), 1L);
        } else {
            list = itemDao.findByTenantIdAndIsActiveOrderByNameAsc(tenantId, 1L);
        }

        return ApiResponse.ok(list.stream().map(e -> {
            ItemDTO dto = toItemDTO(e);
            if ("service".equalsIgnoreCase(e.getItemType())) {
                dto.setCurrentStock(null);
            } else {
                dto.setCurrentStock(stockMap.getOrDefault(e.getId(), e.getOpeningStock()));
            }
            BigDecimal p = e.getPurchasePrice() != null ? e.getPurchasePrice() : BigDecimal.ZERO;
            BigDecimal s = e.getSalePrice() != null ? e.getSalePrice() : BigDecimal.ZERO;
            BigDecimal m = s.subtract(p);
            dto.setMargin(m);
            if (s.compareTo(BigDecimal.ZERO) > 0) {
                dto.setMarginPercent(m.multiply(BigDecimal.valueOf(100)).divide(s, 2, RoundingMode.HALF_UP));
            } else {
                dto.setMarginPercent(BigDecimal.valueOf(100.00));
            }
            return dto;
        }).toList());
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
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";
        
        Integer slCount = 0;
        try {
            slCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM stock_ledger WHERE item_id = ? AND tenant_id = ?",
                    Integer.class, id, tenantId);
        } catch (Exception ignored) {}

        if (slCount != null && slCount > 0) {
            return ApiResponse.error("Item cannot be deleted because it has recorded stock ledger entries.");
        }
        itemDao.deleteById(id);
        return ApiResponse.ok("Item deleted successfully", null);
    }

    @PostMapping("/items/{id}/deactivate")
    public ApiResponse<Void> deactivateItem(@PathVariable Long id) {
        jdbcTemplate.update("UPDATE items SET is_active = 0 WHERE id = ?", id);
        return ApiResponse.ok("Item removed from list", null);
    }

    @GetMapping("/items/{id}/ledger")
    public ApiResponse<Map<String, Object>> getItemLedger(@PathVariable Long id) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";

        ItemEntity item = itemDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found with ID: " + id));

        String locSql = """
            SELECT l.id as location_id, l.code, l.name,
              COALESCE((SELECT sl.balance_qty FROM stock_ledger sl 
                        WHERE sl.item_id = ? AND sl.location_id = l.id AND sl.tenant_id = ? 
                        ORDER BY sl.id DESC LIMIT 1), 0) as qty
            FROM locations l 
            WHERE l.tenant_id = ? AND l.is_active = 1 
            ORDER BY l.code
        """;
        List<Map<String, Object>> byLocation = jdbcTemplate.queryForList(locSql, id, tenantId, tenantId);

        String entrySql = """
            SELECT sl.id, sl.created_at, sl.txn_type, sl.qty, sl.rate, sl.balance_qty,
                   l.code as location_code, l.name as location_name
            FROM stock_ledger sl
            LEFT JOIN locations l ON l.id = sl.location_id
            WHERE sl.item_id = ? AND sl.tenant_id = ?
            ORDER BY sl.id DESC
        """;
        List<Map<String, Object>> entries = jdbcTemplate.queryForList(entrySql, id, tenantId);

        Map<String, Object> result = new HashMap<>();
        result.put("item", toItemDTO(item));
        result.put("byLocation", byLocation);
        result.put("entries", entries);
        return ApiResponse.ok(result);
    }

    @GetMapping("/report")
    public ApiResponse<Map<String, Object>> getInventoryReport() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";

        BigDecimal totalStockValue = BigDecimal.ZERO;
        try {
            totalStockValue = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(qty_remaining * rate), 0) FROM stock_lots",
                BigDecimal.class);
        } catch (Exception ignored) {}

        BigDecimal totalProfit = BigDecimal.ZERO;
        try {
            totalProfit = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM((sale_rate - cost_rate) * qty), 0) FROM stock_lot_consumptions",
                BigDecimal.class);
        } catch (Exception ignored) {}

        List<ItemEntity> goods = itemDao.findByTenantIdAndItemTypeAndIsActiveOrderByNameAsc(tenantId, "goods", 1L);
        List<Map<String, Object>> itemsList = new ArrayList<>();
        for (ItemEntity g : goods) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", g.getId());
            m.put("sku", g.getSku());
            m.put("name", g.getName());
            m.put("category", g.getCategory());
            m.put("brand", g.getBrand());
            m.put("purchasePrice", g.getPurchasePrice());
            m.put("salePrice", g.getSalePrice());
            m.put("reorderLevel", g.getReorderLevel());

            BigDecimal val = BigDecimal.ZERO;
            try {
                val = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(SUM(qty_remaining * rate), 0) FROM stock_lots WHERE item_id = ?",
                    BigDecimal.class, g.getId());
            } catch (Exception ignored) {}
            m.put("stockValue", val != null ? val : BigDecimal.ZERO);

            BigDecimal curStock = BigDecimal.ZERO;
            try {
                curStock = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(balance_qty, 0) FROM stock_ledger WHERE item_id = ? AND tenant_id = ? ORDER BY id DESC LIMIT 1",
                    BigDecimal.class, g.getId(), tenantId);
            } catch (Exception ignored) {}
            m.put("currentStock", curStock != null ? curStock : BigDecimal.ZERO);

            itemsList.add(m);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("totalStockValue", totalStockValue != null ? totalStockValue : BigDecimal.ZERO);
        resp.put("totalProfit", totalProfit != null ? totalProfit : BigDecimal.ZERO);
        resp.put("items", itemsList);
        return ApiResponse.ok(resp);
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
