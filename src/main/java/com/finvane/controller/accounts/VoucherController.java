package com.finvane.controller.accounts;

import com.finvane.model.accounts.VoucherRequestDTO;
import com.finvane.model.common.ApiResponse;
import com.finvane.model.common.PageResponse;
import com.finvane.model.common.TenantContext;
import com.finvane.service.accounts.IVoucherService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/v2/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final IVoucherService voucherService;
    private final JdbcTemplate jdbcTemplate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayBookLineDTO {
        private Long voucherId;
        private String entityGstn;
        private String branchName;
        private String date;
        private String voucherType;
        private String rawVoucherType;
        private String voucherNo;
        private int lineId;
        private String ledgerId;
        private String ledgerName;
        private BigDecimal drAmount;
        private BigDecimal crAmount;
        private String typeOfLedger;
        private String typeOfTax;
        private String taxType;
        private String bankAccountNumber;
        private String gstin;
        private String narration;
        private String status;
        private String createdBy;
        private String approvedBy;
        private String createdAt;
    }

    @PostMapping
    public ApiResponse<VoucherRequestDTO> createVoucher(@Valid @RequestBody VoucherRequestDTO request) {
        return ApiResponse.ok("Voucher posted successfully", voucherService.createVoucher(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<VoucherRequestDTO> getVoucherById(@PathVariable Long id) {
        return ApiResponse.ok(voucherService.getVoucherById(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<VoucherRequestDTO>> searchVouchers(
            @RequestParam(required = false) String voucherType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(voucherService.searchVouchers(voucherType, startDate, endDate, page, size));
    }

    @GetMapping("/daybook")
    public ApiResponse<List<DayBookLineDTO>> getDayBook(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String voucherType,
            @RequestParam(required = false) String voucherNo,
            @RequestParam(required = false, defaultValue = "desc") String dateSort) {

        String tenantId = TenantContext.getTenantId();

        // Get entity info
        String entityName = "Acme Trading Co";
        String entityGstin = "—";
        try {
            Map<String, Object> ent = jdbcTemplate.queryForMap(
                    "SELECT name, gstin FROM entities WHERE id = ? LIMIT 1",
                    Long.parseLong(tenantId)
            );
            if (ent != null) {
                if (ent.get("name") != null) entityName = ent.get("name").toString();
                if (ent.get("gstin") != null) entityGstin = ent.get("gstin").toString();
            }
        } catch (Exception ignored) {}

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT v.id as voucher_id, v.voucher_number, v.voucher_date, v.voucher_type, ")
           .append("       v.narration, v.total_amount, v.created_at, ")
           .append("       vi.id as line_pk, vi.entry_type, vi.amount, vi.line_narration, ")
           .append("       al.code as ledger_code, al.name as ledger_name, ag.nature as account_type ")
           .append("FROM vouchers v ")
           .append("JOIN voucher_items vi ON vi.voucher_id = v.id ")
           .append("JOIN account_ledgers al ON al.id = vi.ledger_id ")
           .append("JOIN account_groups ag ON ag.id = al.group_id ")
           .append("WHERE v.tenant_id = ? ");

        List<Object> params = new ArrayList<>();
        params.add(tenantId);

        if (fromDate != null && !fromDate.isBlank()) {
            sql.append("AND v.voucher_date >= ? ");
            params.add(fromDate);
        }
        if (toDate != null && !toDate.isBlank()) {
            sql.append("AND v.voucher_date <= ? ");
            params.add(toDate);
        }
        if (voucherType != null && !voucherType.isBlank()) {
            sql.append("AND v.voucher_type = ? ");
            params.add(voucherType.toUpperCase());
        }
        if (voucherNo != null && !voucherNo.isBlank()) {
            sql.append("AND v.voucher_number LIKE ? ");
            params.add("%" + voucherNo + "%");
        }
        if (q != null && !q.isBlank()) {
            sql.append("AND (v.voucher_number LIKE ? OR v.voucher_type LIKE ? OR v.narration LIKE ? OR al.name LIKE ? OR al.code LIKE ?) ");
            String like = "%" + q + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }

        String order = "asc".equalsIgnoreCase(dateSort) ? "ASC" : "DESC";
        sql.append("ORDER BY v.voucher_date ").append(order).append(", v.id ").append(order).append(", vi.id ASC LIMIT 2000");

        final String finalEntityName = entityName;
        final String finalEntityGstin = entityGstin;

        Map<Long, Integer> lineCounters = new HashMap<>();

        List<DayBookLineDTO> rows = jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            long vId = rs.getLong("voucher_id");
            int lineId = lineCounters.getOrDefault(vId, 0) + 1;
            lineCounters.put(vId, lineId);

            String vType = rs.getString("voucher_type");
            String displayType = formatVoucherType(vType);

            String entryType = rs.getString("entry_type");
            BigDecimal amt = rs.getBigDecimal("amount");
            BigDecimal dr = "DEBIT".equalsIgnoreCase(entryType) ? amt : null;
            BigDecimal cr = "CREDIT".equalsIgnoreCase(entryType) ? amt : null;

            String nature = rs.getString("account_type");
            String displayNature = nature != null ? nature.substring(0, 1).toUpperCase() + nature.substring(1).toLowerCase() : "Asset";

            return DayBookLineDTO.builder()
                    .voucherId(vId)
                    .entityGstn(finalEntityGstin)
                    .branchName(finalEntityName)
                    .date(rs.getString("voucher_date"))
                    .voucherType(displayType)
                    .rawVoucherType(vType)
                    .voucherNo(rs.getString("voucher_number"))
                    .lineId(lineId)
                    .ledgerId(rs.getString("ledger_code"))
                    .ledgerName(rs.getString("ledger_name"))
                    .drAmount(dr)
                    .crAmount(cr)
                    .typeOfLedger(displayNature)
                    .typeOfTax("")
                    .taxType("")
                    .bankAccountNumber("")
                    .gstin("")
                    .narration(rs.getString("narration"))
                    .status("posted")
                    .createdBy("Admin User")
                    .approvedBy("Admin User")
                    .createdAt(rs.getString("created_at") != null ? rs.getString("created_at").toString() : "")
                    .build();
        }, params.toArray());

        return ApiResponse.ok(rows);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<String> deleteVoucher(@PathVariable Long id) {
        String tenantId = TenantContext.getTenantId();
        jdbcTemplate.update("DELETE FROM voucher_items WHERE voucher_id = ?", id);
        int rows = jdbcTemplate.update("DELETE FROM vouchers WHERE id = ? AND tenant_id = ?", id, tenantId);
        if (rows > 0) {
            return ApiResponse.ok("Voucher deleted successfully.");
        } else {
            throw new IllegalArgumentException("Voucher not found with ID: " + id);
        }
    }

    @PostMapping("/delete-all")
    @Transactional
    public ApiResponse<String> deleteAll(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        String tenantId = TenantContext.getTenantId();

        StringBuilder sql = new StringBuilder("DELETE vi FROM voucher_items vi JOIN vouchers v ON v.id = vi.voucher_id WHERE v.tenant_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(tenantId);
        if (fromDate != null && !fromDate.isBlank()) {
            sql.append(" AND v.voucher_date >= ?");
            params.add(fromDate);
        }
        if (toDate != null && !toDate.isBlank()) {
            sql.append(" AND v.voucher_date <= ?");
            params.add(toDate);
        }
        jdbcTemplate.update(sql.toString(), params.toArray());

        StringBuilder vSql = new StringBuilder("DELETE FROM vouchers WHERE tenant_id = ?");
        List<Object> vParams = new ArrayList<>();
        vParams.add(tenantId);
        if (fromDate != null && !fromDate.isBlank()) {
            vSql.append(" AND voucher_date >= ?");
            vParams.add(fromDate);
        }
        if (toDate != null && !toDate.isBlank()) {
            vSql.append(" AND voucher_date <= ?");
            vParams.add(toDate);
        }
        int count = jdbcTemplate.update(vSql.toString(), vParams.toArray());

        return ApiResponse.ok("Deleted " + count + " vouchers.");
    }

    private static String formatVoucherType(String vType) {
        if (vType == null) return "Journal";
        return switch (vType.toUpperCase()) {
            case "PAYMENT" -> "Payment";
            case "RECEIPT" -> "Receipt";
            case "CONTRA" -> "Contra";
            case "JOURNAL" -> "Journal";
            case "INVOICE" -> "Invoice";
            case "SALES" -> "Sales";
            case "PURCHASE" -> "Purchase";
            case "CREDIT_NOTE" -> "Credit Note";
            case "DEBIT_NOTE" -> "Debit Note";
            case "PAYROLL" -> "Payroll";
            case "DEPRECIATION" -> "Depreciation";
            case "STOCK_TRANSFER" -> "Stock Transfer";
            case "ASSET_ACQUISITION" -> "Asset Acquisition";
            default -> vType;
        };
    }
}
