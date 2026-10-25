package com.finvane.controller.invoices;

import com.finvane.model.common.ApiResponse;
import com.finvane.model.common.TenantContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/v2/notes")
@RequiredArgsConstructor
public class NoteController {

    private final JdbcTemplate jdbcTemplate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoteDTO {
        private Long id;
        private String noteNo;
        private String noteType; // credit or debit
        private Long invoiceId;
        private String invoiceNo;
        private Long partyId;
        private String partyName;
        private Long locationId;
        private String noteDate;
        private String reason;
        private BigDecimal taxableValue;
        private BigDecimal cgst;
        private BigDecimal sgst;
        private BigDecimal igst;
        private BigDecimal totalAmount;
        private String status;
        private Long voucherId;
        private List<NoteLineDTO> lines;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoteLineDTO {
        private Long id;
        private Long itemId;
        private String itemName;
        private String sku;
        private BigDecimal qty;
        private BigDecimal rate;
        private BigDecimal taxableValue;
        private BigDecimal gstRate;
        private BigDecimal cgst;
        private BigDecimal sgst;
        private BigDecimal igst;
        private BigDecimal amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PickInvoiceDTO {
        private Long id;
        private String invoiceNo;
        private String invoiceDate;
        private String partyName;
        private BigDecimal totalAmount;
    }

    @GetMapping
    public ApiResponse<List<NoteDTO>> getNotes(@RequestParam(required = false, defaultValue = "credit") String type) {
        String tenantId = TenantContext.getTenantId();
        boolean isCredit = "credit".equalsIgnoreCase(type);
        String table = isCredit ? "credit_notes" : "debit_notes";
        String idCol = isCredit ? "cn_no" : "dn_no";
        String dateCol = isCredit ? "cn_date" : "dn_date";

        String sql = String.format(
                "SELECT n.id, n.%s as note_no, n.invoice_id, n.party_id, p.name as party_name, " +
                "       n.location_id, n.%s as note_date, n.reason, n.taxable_value, n.cgst, n.sgst, n.igst, " +
                "       n.total_amount, n.status, n.voucher_id, i.invoice_no " +
                "FROM %s n " +
                "LEFT JOIN parties p ON p.id = n.party_id " +
                "LEFT JOIN invoices i ON i.id = n.invoice_id " +
                "WHERE n.tenant_id = ? " +
                "ORDER BY n.id DESC LIMIT 500",
                idCol, dateCol, table
        );

        List<NoteDTO> list = jdbcTemplate.query(sql, (rs, rowNum) -> NoteDTO.builder()
                .id(rs.getLong("id"))
                .noteNo(rs.getString("note_no"))
                .noteType(isCredit ? "credit" : "debit")
                .invoiceId(rs.getLong("invoice_id"))
                .invoiceNo(rs.getString("invoice_no"))
                .partyId(rs.getLong("party_id"))
                .partyName(rs.getString("party_name") != null ? rs.getString("party_name") : "Party #" + rs.getLong("party_id"))
                .locationId(rs.getLong("location_id"))
                .noteDate(rs.getString("note_date"))
                .reason(rs.getString("reason"))
                .taxableValue(rs.getBigDecimal("taxable_value"))
                .cgst(rs.getBigDecimal("cgst"))
                .sgst(rs.getBigDecimal("sgst"))
                .igst(rs.getBigDecimal("igst"))
                .totalAmount(rs.getBigDecimal("total_amount"))
                .status(rs.getString("status") != null ? rs.getString("status") : "posted")
                .voucherId(rs.getObject("voucher_id") != null ? rs.getLong("voucher_id") : null)
                .build(), tenantId);

        return ApiResponse.ok(list);
    }

    @GetMapping("/invoices")
    public ApiResponse<List<PickInvoiceDTO>> getInvoicesToPick(@RequestParam(required = false, defaultValue = "credit") String type) {
        String tenantId = TenantContext.getTenantId();
        String invType = "credit".equalsIgnoreCase(type) ? "sales" : "purchase";

        String sql = "SELECT i.id, i.invoice_no, i.invoice_date, p.name as party_name, i.total_amount " +
                     "FROM invoices i " +
                     "LEFT JOIN parties p ON p.id = i.party_id " +
                     "WHERE i.tenant_id = ? AND i.invoice_type = ? " +
                     "ORDER BY i.id DESC LIMIT 100";

        List<PickInvoiceDTO> list = jdbcTemplate.query(sql, (rs, rowNum) -> PickInvoiceDTO.builder()
                .id(rs.getLong("id"))
                .invoiceNo(rs.getString("invoice_no"))
                .invoiceDate(rs.getString("invoice_date"))
                .partyName(rs.getString("party_name") != null ? rs.getString("party_name") : "Party")
                .totalAmount(rs.getBigDecimal("total_amount"))
                .build(), tenantId, invType);

        return ApiResponse.ok(list);
    }

    @GetMapping("/invoice/{id}")
    public ApiResponse<Map<String, Object>> getInvoiceForNote(@PathVariable Long id) {
        String tenantId = TenantContext.getTenantId();

        String invSql = "SELECT i.id, i.invoice_no, i.invoice_type, i.invoice_date, i.party_id, p.name as party_name, " +
                        "       i.location_id, i.total_amount, i.place_of_supply " +
                        "FROM invoices i " +
                        "LEFT JOIN parties p ON p.id = i.party_id " +
                        "WHERE i.id = ? AND i.tenant_id = ?";

        List<Map<String, Object>> invList = jdbcTemplate.queryForList(invSql, id, tenantId);
        if (invList.isEmpty()) {
            throw new IllegalArgumentException("Invoice not found with ID: " + id);
        }
        Map<String, Object> invoice = invList.get(0);

        String linesSql = "SELECT il.id, il.item_id, it.name as item_name, it.sku, il.qty, il.rate, " +
                          "       il.taxable_value, il.gst_rate, il.cgst, il.sgst, il.igst, il.total_amount " +
                          "FROM invoice_lines il " +
                          "LEFT JOIN items it ON it.id = il.item_id " +
                          "WHERE il.invoice_id = ? AND il.tenant_id = ? ORDER BY il.id ASC";

        List<Map<String, Object>> lines = jdbcTemplate.queryForList(linesSql, id, tenantId);

        Map<String, Object> res = new HashMap<>();
        res.put("invoice", invoice);
        res.put("lines", lines);
        return ApiResponse.ok(res);
    }

    @PostMapping
    @Transactional
    public ApiResponse<NoteDTO> createNote(@RequestBody NoteDTO dto) {
        String tenantId = TenantContext.getTenantId();
        boolean isCredit = "credit".equalsIgnoreCase(dto.getNoteType());
        String table = isCredit ? "credit_notes" : "debit_notes";
        String lineTable = isCredit ? "credit_note_lines" : "debit_note_lines";
        String idCol = isCredit ? "cn_no" : "dn_no";
        String dateCol = isCredit ? "cn_date" : "dn_date";
        String fkCol = isCredit ? "cn_id" : "dn_id";
        String prefix = isCredit ? "CN" : "DN";

        // Count existing notes for sequence
        Integer cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + table + " WHERE tenant_id = ?",
                Integer.class, tenantId
        );
        String noteNo = String.format("%s-%05d", prefix, (cnt != null ? cnt : 0) + 1);

        String noteDate = dto.getNoteDate() != null && !dto.getNoteDate().isBlank()
                ? dto.getNoteDate() : LocalDate.now().toString();

        BigDecimal totalTaxable = BigDecimal.ZERO;
        BigDecimal totalCgst = BigDecimal.ZERO;
        BigDecimal totalSgst = BigDecimal.ZERO;
        BigDecimal totalIgst = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        List<NoteLineDTO> inputLines = dto.getLines() != null ? dto.getLines() : Collections.emptyList();
        List<NoteLineDTO> processedLines = new ArrayList<>();

        for (NoteLineDTO l : inputLines) {
            if (l.getQty() == null || l.getQty().compareTo(BigDecimal.ZERO) <= 0) continue;
            BigDecimal rate = l.getRate() != null ? l.getRate() : BigDecimal.ZERO;
            BigDecimal taxable = l.getQty().multiply(rate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal gstRate = l.getGstRate() != null ? l.getGstRate() : BigDecimal.valueOf(18);

            BigDecimal cgst = l.getCgst() != null ? l.getCgst() : BigDecimal.ZERO;
            BigDecimal sgst = l.getSgst() != null ? l.getSgst() : BigDecimal.ZERO;
            BigDecimal igst = l.getIgst() != null ? l.getIgst() : BigDecimal.ZERO;

            if (cgst.compareTo(BigDecimal.ZERO) == 0 && sgst.compareTo(BigDecimal.ZERO) == 0 && igst.compareTo(BigDecimal.ZERO) == 0) {
                // calculate 50/50 intra-state by default
                BigDecimal halfGst = gstRate.divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
                cgst = taxable.multiply(halfGst).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                sgst = taxable.multiply(halfGst).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
            BigDecimal lineTotal = taxable.add(cgst).add(sgst).add(igst);

            totalTaxable = totalTaxable.add(taxable);
            totalCgst = totalCgst.add(cgst);
            totalSgst = totalSgst.add(sgst);
            totalIgst = totalIgst.add(igst);
            totalAmount = totalAmount.add(lineTotal);

            l.setTaxableValue(taxable);
            l.setCgst(cgst);
            l.setSgst(sgst);
            l.setIgst(igst);
            l.setAmount(lineTotal);
            processedLines.add(l);
        }

        // Auto post voucher
        String voucherType = isCredit ? "CREDIT_NOTE" : "DEBIT_NOTE";
        String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String vSql = "INSERT INTO vouchers (tenant_id, voucher_number, voucher_date, voucher_type, narration, total_amount, created_at) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(vSql, tenantId, noteNo + "-JV", noteDate, voucherType,
                (isCredit ? "Credit Note " : "Debit Note ") + noteNo + ": " + (dto.getReason() != null ? dto.getReason() : ""),
                totalAmount, nowStr);
        Long voucherId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

        // Insert into note table
        String insertNoteSql = String.format(
                "INSERT INTO %s (%s, invoice_id, party_id, location_id, %s, reason, " +
                "               taxable_value, cgst, sgst, igst, total_amount, status, voucher_id, created_at, tenant_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'posted', ?, ?, ?)",
                table, idCol, dateCol
        );
        jdbcTemplate.update(insertNoteSql,
                noteNo, dto.getInvoiceId(), dto.getPartyId(), dto.getLocationId(),
                noteDate, dto.getReason(), totalTaxable, totalCgst, totalSgst, totalIgst,
                totalAmount, voucherId, nowStr, tenantId
        );
        Long noteId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

        // Insert note lines
        String insertLineSql = String.format(
                "INSERT INTO %s (%s, item_id, qty, rate, taxable_value, gst_rate, cgst, sgst, igst, amount, tenant_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                lineTable, fkCol
        );
        for (NoteLineDTO l : processedLines) {
            jdbcTemplate.update(insertLineSql,
                    noteId, l.getItemId(), l.getQty(), l.getRate(), l.getTaxableValue(),
                    l.getGstRate(), l.getCgst(), l.getSgst(), l.getIgst(), l.getAmount(), tenantId
            );
        }

        dto.setId(noteId);
        dto.setNoteNo(noteNo);
        dto.setNoteDate(noteDate);
        dto.setTaxableValue(totalTaxable);
        dto.setCgst(totalCgst);
        dto.setSgst(totalSgst);
        dto.setIgst(totalIgst);
        dto.setTotalAmount(totalAmount);
        dto.setStatus("posted");
        dto.setVoucherId(voucherId);
        dto.setLines(processedLines);

        return ApiResponse.ok(dto);
    }
}
