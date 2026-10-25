package com.finvane.controller.accounts;

import com.finvane.dao.accounts.IAccountGroupJpaAccess;
import com.finvane.entity.accounts.AccountGroupEntity;
import com.finvane.model.accounts.AccountLedgerDTO;
import com.finvane.model.common.ApiResponse;
import com.finvane.model.common.PageResponse;
import com.finvane.model.common.TenantContext;
import com.finvane.service.accounts.ILedgerService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/v2/accounts")
@RequiredArgsConstructor
public class ChartOfAccountsController {

    private final ILedgerService ledgerService;
    private final IAccountGroupJpaAccess groupDao;
    private final JdbcTemplate jdbcTemplate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountDetailDTO {
        private Long id;
        private String code;
        private String name;
        private BigDecimal openingBalance;
        private String type;
        private String ledgerGroup1;
        private String ledgerGroup2;
        private String ledgerGroup3;
        private String ledgerGroup4;
        private String ledgerCode;
        private String plType;
        private String sectorReportLine;
        private String gstApplicable;
        private String tdsApplicable;
        private String tcsApplicable;
        private String gstTypeOfSupply;
        private String gstNatureOfSupply;
        private String tdsSectionNumber;
        private String tcsSectionNumber;
        private String hsnCode;
        private BigDecimal igstRate;
        private BigDecimal cgstRate;
        private BigDecimal sgstRate;
        private BigDecimal cessRate;
        private String rcmApplicable;
        private String itcEligible;
        private String branchName;
        private String entityGstin;
        private Boolean isActive;
        private BigDecimal currentBalance;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartOfAccountsResponseDTO {
        private List<AccountDetailDTO> accounts;
        private boolean inventoryEnabled;
        private BigDecimal openingBalanceTotal;
        private boolean isBalanced;
        private int total;
    }

    @GetMapping
    public ApiResponse<ChartOfAccountsResponseDTO> getChartOfAccounts(
            @RequestParam(required = false) String q) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";

        boolean inventoryEnabled = isInventoryEnabled(tenantId);

        String sql = "SELECT " +
                "  l.id, l.code, l.name, COALESCE(l.opening_balance, 0) as opening_balance, l.is_active, " +
                "  COALESCE(l.current_balance, 0) as current_balance, l.sector_report_line, " +
                "  g.id as group_id, g.name as group_name, g.nature as group_nature, g.parent_id, " +
                "  pg.name as parent_group_name, " +
                "  e.gstin as entity_gstin " +
                "FROM account_ledgers l " +
                "LEFT JOIN account_groups g ON g.id = l.group_id " +
                "LEFT JOIN account_groups pg ON pg.id = g.parent_id " +
                "LEFT JOIN entities e ON e.tenant_id = l.tenant_id " +
                "WHERE l.tenant_id = ? " +
                (q != null && !q.isBlank() ? "  AND (l.code LIKE ? OR l.name LIKE ?) " : "") +
                "ORDER BY CAST(l.code AS UNSIGNED), l.code";

        List<Object> params = new ArrayList<>();
        params.add(tenantId);
        if (q != null && !q.isBlank()) {
            params.add("%" + q.trim() + "%");
            params.add("%" + q.trim() + "%");
        }

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params.toArray());

        BigDecimal openingBalanceTotal = BigDecimal.ZERO;
        List<AccountDetailDTO> accounts = new ArrayList<>();

        for (Map<String, Object> r : rows) {
            BigDecimal op = r.get("opening_balance") != null ? new BigDecimal(r.get("opening_balance").toString()) : BigDecimal.ZERO;
            openingBalanceTotal = openingBalanceTotal.add(op);

            String nature = r.get("group_nature") != null ? r.get("group_nature").toString() : "";
            String type = formatNature(nature);

            String parentName = r.get("parent_group_name") != null ? r.get("parent_group_name").toString() : null;
            String groupName = r.get("group_name") != null ? r.get("group_name").toString() : "General";

            String ledgerGroup1 = parentName != null ? parentName : groupName;
            String ledgerGroup2 = parentName != null ? groupName : null;

            Number idNum = (Number) r.get("id");
            Object activeObj = r.get("is_active");
            boolean isActive = true;
            if (activeObj instanceof Boolean) {
                isActive = (Boolean) activeObj;
            } else if (activeObj instanceof Number) {
                isActive = ((Number) activeObj).intValue() == 1;
            } else if (activeObj instanceof byte[]) {
                byte[] b = (byte[]) activeObj;
                isActive = b.length > 0 && b[0] == 1;
            }

            accounts.add(AccountDetailDTO.builder()
                    .id(idNum != null ? idNum.longValue() : null)
                    .code(r.get("code") != null ? r.get("code").toString() : "")
                    .name(r.get("name") != null ? r.get("name").toString() : "")
                    .openingBalance(op)
                    .type(type)
                    .ledgerGroup1(ledgerGroup1)
                    .ledgerGroup2(ledgerGroup2)
                    .ledgerGroup3(null)
                    .ledgerGroup4(null)
                    .ledgerCode(null)
                    .plType(null)
                    .sectorReportLine(r.get("sector_report_line") != null ? r.get("sector_report_line").toString() : null)
                    .gstApplicable(null)
                    .tdsApplicable(null)
                    .tcsApplicable(null)
                    .gstTypeOfSupply(null)
                    .gstNatureOfSupply(null)
                    .tdsSectionNumber(null)
                    .tcsSectionNumber(null)
                    .hsnCode(null)
                    .igstRate(null)
                    .cgstRate(null)
                    .sgstRate(null)
                    .cessRate(null)
                    .rcmApplicable(null)
                    .itcEligible(null)
                    .branchName(null)
                    .entityGstin(r.get("entity_gstin") != null ? r.get("entity_gstin").toString() : null)
                    .isActive(isActive)
                    .currentBalance(r.get("current_balance") != null ? new BigDecimal(r.get("current_balance").toString()) : BigDecimal.ZERO)
                    .build());
        }

        boolean isBalanced = openingBalanceTotal.abs().doubleValue() < 0.01;

        return ApiResponse.ok(ChartOfAccountsResponseDTO.builder()
                .accounts(accounts)
                .inventoryEnabled(inventoryEnabled)
                .openingBalanceTotal(openingBalanceTotal)
                .isBalanced(isBalanced)
                .total(accounts.size())
                .build());
    }

    @GetMapping("/settings/inventory")
    public ApiResponse<Map<String, Object>> getInventorySetting() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";
        return ApiResponse.ok(Map.of("inventoryEnabled", isInventoryEnabled(tenantId)));
    }

    @PostMapping("/settings/inventory")
    public ApiResponse<Map<String, Object>> updateInventorySetting(@RequestBody Map<String, Object> body) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";

        Object val = body.get("inventory_enabled");
        if (val == null) val = body.get("inventoryEnabled");
        boolean enabled = true;
        if (val instanceof Boolean) {
            enabled = (Boolean) val;
        } else if (val != null) {
            enabled = "1".equals(val.toString()) || "true".equalsIgnoreCase(val.toString());
        }

        try {
            jdbcTemplate.update(
                    "INSERT INTO app_settings (tenant_id, setting_key, setting_value) VALUES (?, 'inventory_enabled', ?) " +
                    "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)",
                    tenantId, enabled ? "1" : "0"
            );
        } catch (Exception e) {
            log.error("Error updating inventory_enabled setting: {}", e.getMessage(), e);
        }

        return ApiResponse.ok("Inventory setting updated", Map.of("inventoryEnabled", enabled));
    }

    @PostMapping("/{id}/opening-balance")
    public ApiResponse<Boolean> updateOpeningBalance(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";

        Object amtObj = body.get("opening_balance");
        if (amtObj == null) amtObj = body.get("openingBalance");
        BigDecimal amount = BigDecimal.ZERO;
        if (amtObj != null) {
            try {
                amount = new BigDecimal(amtObj.toString());
            } catch (Exception ignored) {}
        }

        int updated = jdbcTemplate.update(
                "UPDATE account_ledgers SET opening_balance = ? WHERE id = ? AND tenant_id = ?",
                amount, id, tenantId
        );

        return ApiResponse.ok("Opening balance updated successfully", updated > 0);
    }

    @PostMapping("/{id}/toggle")
    public ApiResponse<Boolean> toggleAccountStatus(@PathVariable Long id) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";

        int updated = jdbcTemplate.update(
                "UPDATE account_ledgers SET is_active = NOT is_active WHERE id = ? AND tenant_id = ?",
                id, tenantId
        );

        return ApiResponse.ok("Ledger status updated", updated > 0);
    }

    @PostMapping("/{id}/delete")
    public ApiResponse<Boolean> deleteAccount(@PathVariable Long id) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";

        try {
            Integer voucherCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM voucher_items WHERE ledger_id = ?",
                    Integer.class, id
            );
            if (voucherCount != null && voucherCount > 0) {
                return ApiResponse.error("This ledger has " + voucherCount + " posted transaction line(s) and cannot be deleted.");
            }

            jdbcTemplate.update("DELETE FROM account_ledgers WHERE id = ? AND tenant_id = ?", id, tenantId);
            return ApiResponse.ok("Ledger deleted successfully", true);
        } catch (Exception e) {
            return ApiResponse.error("Failed to delete ledger: " + e.getMessage());
        }
    }

    @GetMapping("/groups")
    public ApiResponse<List<AccountGroupEntity>> getAccountGroups() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";
        return ApiResponse.ok(groupDao.findByTenantIdOrderByNameAsc(tenantId));
    }

    @GetMapping("/ledgers")
    public ApiResponse<PageResponse<AccountLedgerDTO>> getLedgers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ApiResponse.ok(ledgerService.searchLedgers(search, groupId, page, size));
    }

    @GetMapping("/ledgers/all")
    public ApiResponse<List<AccountLedgerDTO>> getAllActiveLedgers() {
        return ApiResponse.ok(ledgerService.getAllActiveLedgers());
    }

    @GetMapping("/ledgers/{id}")
    public ApiResponse<AccountLedgerDTO> getLedgerById(@PathVariable Long id) {
        return ApiResponse.ok(ledgerService.getLedgerById(id));
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectorMappingRowDTO {
        private Long id;
        private String code;
        private String name;
        private String type;
        private String g1;
        private String g2;
        private Long groupId;
        private BigDecimal amount;
        private String saved;
        private String effective;
        private String effectiveLabel;
        private String source;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectorGroupRuleDTO {
        private Long id;
        private String label;
        private String type;
        private int count;
        private String rule;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectorMappingResponseDTO {
        private Map<String, Object> entity;
        private Map<String, Object> template;
        private Map<String, Object> counts;
        private List<SectorGroupRuleDTO> groups;
        private List<SectorMappingRowDTO> rows;
        private Map<String, List<Map<String, String>>> lineOptions;
    }

    @GetMapping("/sector-mapping")
    public ApiResponse<SectorMappingResponseDTO> getSectorMapping() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";

        String entityName = "Acme Trading Co";
        try {
            Map<String, Object> ent = jdbcTemplate.queryForMap(
                    "SELECT name FROM entities WHERE tenant_id = ? LIMIT 1",
                    tenantId
            );
            if (ent.get("name") != null) entityName = ent.get("name").toString();
        } catch (Exception ignored) {}

        List<Map<String, String>> incomeLines = List.of(
                Map.of("key", "revenue", "label", "Revenue from Operations"),
                Map.of("key", "other_income", "label", "Other Income")
        );
        List<Map<String, String>> expenseLines = List.of(
                Map.of("key", "materials", "label", "Purchases of Stock-in-Trade"),
                Map.of("key", "direct_cost", "label", "Direct Expenses (freight inward etc.)"),
                Map.of("key", "employee", "label", "Employee Cost"),
                Map.of("key", "selling", "label", "Selling & Distribution"),
                Map.of("key", "admin", "label", "Administrative & Other"),
                Map.of("key", "depreciation", "label", "Depreciation & Amortisation"),
                Map.of("key", "finance", "label", "Finance Cost")
        );
        Map<String, List<Map<String, String>>> lineOptions = Map.of(
                "income", incomeLines,
                "expense", expenseLines
        );

        Map<String, String> lineLabels = new HashMap<>();
        incomeLines.forEach(l -> lineLabels.put(l.get("key"), l.get("label")));
        expenseLines.forEach(l -> lineLabels.put(l.get("key"), l.get("label")));

        Map<Long, BigDecimal> amounts = new HashMap<>();
        List<Map<String, Object>> amtRows = jdbcTemplate.queryForList(
                "SELECT vi.ledger_id, " +
                "  COALESCE(SUM(CASE WHEN vi.entry_type='DEBIT' THEN vi.amount ELSE 0 END), 0) as dr, " +
                "  COALESCE(SUM(CASE WHEN vi.entry_type='CREDIT' THEN vi.amount ELSE 0 END), 0) as cr " +
                "FROM voucher_items vi " +
                "JOIN vouchers v ON v.id = vi.voucher_id " +
                "WHERE v.tenant_id = ? " +
                "GROUP BY vi.ledger_id",
                tenantId
        );
        for (Map<String, Object> ar : amtRows) {
            Number lid = (Number) ar.get("ledger_id");
            BigDecimal dr = ar.get("dr") != null ? new BigDecimal(ar.get("dr").toString()) : BigDecimal.ZERO;
            BigDecimal cr = ar.get("cr") != null ? new BigDecimal(ar.get("cr").toString()) : BigDecimal.ZERO;
            if (lid != null) {
                amounts.put(lid.longValue(), dr.subtract(cr));
            }
        }

        Map<Long, String> groupRules = new HashMap<>();
        List<Map<String, Object>> ruleRows = jdbcTemplate.queryForList(
                "SELECT group_id, line FROM sector_group_rules WHERE tenant_id = ?",
                tenantId
        );
        for (Map<String, Object> rr : ruleRows) {
            Number gid = (Number) rr.get("group_id");
            String line = rr.get("line") != null ? rr.get("line").toString() : null;
            if (gid != null && line != null) groupRules.put(gid.longValue(), line);
        }

        List<Map<String, Object>> accRows = jdbcTemplate.queryForList(
                "SELECT a.id, a.code, a.name, g.nature as type, a.group_id, a.sector_report_line, " +
                "  g.name as group_name, g.parent_id as group_parent_id, " +
                "  pg.name as primary_group_name " +
                "FROM account_ledgers a " +
                "LEFT JOIN account_groups g ON g.id = a.group_id " +
                "LEFT JOIN account_groups pg ON pg.id = g.parent_id " +
                "WHERE a.tenant_id = ? AND g.nature IN ('INCOME', 'EXPENSE') " +
                "ORDER BY CAST(a.code AS UNSIGNED), a.code",
                tenantId
        );

        List<SectorMappingRowDTO> rows = new ArrayList<>();
        Map<Long, SectorGroupRuleDTO> groupMap = new LinkedHashMap<>();

        for (Map<String, Object> ar : accRows) {
            Long id = ((Number) ar.get("id")).longValue();
            String code = ar.get("code") != null ? ar.get("code").toString() : "";
            String name = ar.get("name") != null ? ar.get("name").toString() : "";
            String nature = ar.get("type") != null ? ar.get("type").toString().toLowerCase() : "expense";
            Long groupId = ar.get("group_id") != null ? ((Number) ar.get("group_id")).longValue() : null;
            String saved = ar.get("sector_report_line") != null ? ar.get("sector_report_line").toString() : "";

            String groupName = ar.get("group_name") != null ? ar.get("group_name").toString() : "General";
            String parentName = ar.get("primary_group_name") != null ? ar.get("primary_group_name").toString() : null;
            String g1 = parentName != null ? parentName : groupName;
            String g2 = parentName != null ? groupName : "";

            BigDecimal netDebit = amounts.getOrDefault(id, BigDecimal.ZERO);
            BigDecimal balance = "income".equals(nature) ? netDebit.negate() : netDebit;

            String rule = groupId != null ? groupRules.get(groupId) : null;
            String effective;
            String source;

            if (saved != null && !saved.isBlank()) {
                effective = saved;
                source = "ledger";
            } else if (rule != null && !rule.isBlank()) {
                effective = rule;
                source = "group";
            } else {
                if ("income".equals(nature)) {
                    effective = "revenue";
                } else {
                    if ("Purchase Accounts".equalsIgnoreCase(g1)) {
                        effective = "materials";
                    } else {
                        effective = "admin";
                    }
                }
                source = "default";
            }

            String effectiveLabel = lineLabels.getOrDefault(effective, effective);

            rows.add(SectorMappingRowDTO.builder()
                    .id(id)
                    .code(code)
                    .name(name)
                    .type(nature)
                    .g1(g1)
                    .g2(g2)
                    .groupId(groupId)
                    .amount(balance)
                    .saved(saved)
                    .effective(effective)
                    .effectiveLabel(effectiveLabel)
                    .source(source)
                    .status("mapped")
                    .build());

            if (groupId != null) {
                SectorGroupRuleDTO grpDto = groupMap.computeIfAbsent(groupId, k -> SectorGroupRuleDTO.builder()
                        .id(k)
                        .label(g2 != null && !g2.isBlank() ? g1 + " › " + g2 : g1)
                        .type(nature)
                        .count(0)
                        .rule(rule != null ? rule : "")
                        .build());
                grpDto.setCount(grpDto.getCount() + 1);
            }
        }

        List<SectorGroupRuleDTO> groups = new ArrayList<>(groupMap.values());
        groups.sort(Comparator.comparing(SectorGroupRuleDTO::getLabel));

        return ApiResponse.ok(SectorMappingResponseDTO.builder()
                .entity(Map.of("name", entityName))
                .template(Map.of("key", "trading", "label", "Trading"))
                .counts(Map.of("all", rows.size(), "mapped", rows.size()))
                .groups(groups)
                .rows(rows)
                .lineOptions(lineOptions)
                .build());
    }

    @PostMapping("/sector-mapping/save")
    public ApiResponse<Boolean> saveSectorMapping(@RequestBody Map<String, Object> body) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";

        String line = body.get("line") != null ? body.get("line").toString().trim() : null;
        if (line != null && line.isBlank()) line = null;

        Object idsObj = body.get("ids");
        List<Long> ids = new ArrayList<>();
        if (idsObj instanceof List) {
            for (Object o : (List<?>) idsObj) {
                if (o != null) {
                    try { ids.add(Long.valueOf(o.toString())); } catch (Exception ignored) {}
                }
            }
        } else if ("filtered".equals(body.get("scope"))) {
            List<Long> allIds = jdbcTemplate.queryForList(
                    "SELECT a.id FROM account_ledgers a " +
                    "JOIN account_groups g ON g.id = a.group_id " +
                    "WHERE a.tenant_id = ? AND g.nature IN ('INCOME', 'EXPENSE')",
                    Long.class, tenantId
            );
            ids.addAll(allIds);
        }

        for (Long id : ids) {
            jdbcTemplate.update(
                    "UPDATE account_ledgers SET sector_report_line = ? WHERE id = ? AND tenant_id = ?",
                    line, id, tenantId
            );
        }

        return ApiResponse.ok("Sector mapping saved successfully", true);
    }

    @PostMapping("/sector-mapping/group")
    public ApiResponse<Boolean> saveSectorGroupRule(@RequestBody Map<String, Object> body) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";

        Object gidObj = body.get("group_id");
        Long groupId = null;
        if (gidObj != null) {
            try { groupId = Long.valueOf(gidObj.toString()); } catch (Exception ignored) {}
        }
        if (groupId == null) return ApiResponse.error("Invalid group id");

        String line = body.get("line") != null ? body.get("line").toString().trim() : null;
        if (line == null || line.isBlank()) {
            jdbcTemplate.update(
                    "DELETE FROM sector_group_rules WHERE tenant_id = ? AND group_id = ?",
                    tenantId, groupId
            );
        } else {
            jdbcTemplate.update(
                    "INSERT INTO sector_group_rules (tenant_id, group_id, line) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE line = VALUES(line)",
                    tenantId, groupId, line
            );
        }

        return ApiResponse.ok("Group rule updated successfully", true);
    }

    private boolean isInventoryEnabled(String tenantId) {
        try {
            String val = jdbcTemplate.queryForObject(
                    "SELECT setting_value FROM app_settings WHERE tenant_id = ? AND setting_key = 'inventory_enabled'",
                    String.class, tenantId
            );
            return val == null || !"0".equals(val.trim());
        } catch (Exception e) {
            return true;
        }
    }

    private String formatNature(String nature) {
        if (nature == null || nature.isBlank()) return "Asset";
        return switch (nature.toUpperCase()) {
            case "ASSET" -> "Asset";
            case "LIABILITY" -> "Liability";
            case "EQUITY" -> "Equity";
            case "INCOME" -> "Income";
            case "EXPENSE" -> "Expense";
            default -> nature.substring(0, 1).toUpperCase() + nature.substring(1).toLowerCase();
        };
    }
}
