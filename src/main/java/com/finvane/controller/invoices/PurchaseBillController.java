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
@RequestMapping("/api/v2/purchase-bills")
@RequiredArgsConstructor
public class PurchaseBillController {

    private final JdbcTemplate jdbcTemplate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseBillSummaryDTO {
        private int pendingRcm;
        private BigDecimal pendingRcmAmount;
        private int pendingItc;
        private BigDecimal pendingItcAmount;
        private int ledgerCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseBillFormDTO {
        private Long id;
        private Long partyId;
        private String billNo;
        private String billDate;
        private String dueDate;
        private String narration;
        private List<PurchaseBillLineDTO> lines;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseBillLineDTO {
        private String desc;
        private String hsn;
        private BigDecimal qty;
        private BigDecimal rate;
        private BigDecimal discount;
        private BigDecimal gstPct;
        private String gstLedger;
        private String ledgerName;
        private String rcm; // "Yes" / "No"
        private String isImport; // "Yes" / "No"
        private String itc; // "Eligible" / "Ineligible..."
    }

    @GetMapping
    public ApiResponse<PurchaseBillSummaryDTO> getSummary() {
        String tenantId = TenantContext.getTenantId();

        Integer ledgerCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM account_ledgers al " +
                "JOIN account_groups ag ON ag.id = al.group_id " +
                "WHERE al.tenant_id = ? AND al.is_active = 1 AND ag.nature = 'EXPENSE'",
                Integer.class, tenantId
        );

        // Check pending RCM bills
        Integer pendingRcm = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM invoices WHERE tenant_id = ? AND invoice_type = 'purchase' AND rcm_tax > 0",
                Integer.class, tenantId
        );
        BigDecimal pendingRcmAmount = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(rcm_tax), 0) FROM invoices WHERE tenant_id = ? AND invoice_type = 'purchase'",
                BigDecimal.class, tenantId
        );

        return ApiResponse.ok(PurchaseBillSummaryDTO.builder()
                .pendingRcm(pendingRcm != null ? pendingRcm : 0)
                .pendingRcmAmount(pendingRcmAmount != null ? pendingRcmAmount : BigDecimal.ZERO)
                .pendingItc(0)
                .pendingItcAmount(BigDecimal.ZERO)
                .ledgerCount(ledgerCount != null ? ledgerCount : 48)
                .build());
    }

    @GetMapping("/meta")
    public ApiResponse<Map<String, Object>> getMeta() {
        String tenantId = TenantContext.getTenantId();

        List<Map<String, Object>> vendors = jdbcTemplate.queryForList(
                "SELECT id, name, gstin, state_code FROM parties WHERE tenant_id = ? AND party_type = 'vendor' AND is_active = 1 ORDER BY name ASC",
                tenantId
        );

        List<Map<String, Object>> ledgers = jdbcTemplate.queryForList(
                "SELECT al.id, al.code, al.name, ag.nature FROM account_ledgers al " +
                "JOIN account_groups ag ON ag.id = al.group_id " +
                "WHERE al.tenant_id = ? AND al.is_active = 1 AND (ag.nature = 'EXPENSE' OR ag.nature = 'ASSET') ORDER BY al.name ASC",
                tenantId
        );

        Map<String, Object> meta = new HashMap<>();
        meta.put("vendors", vendors);
        meta.put("ledgers", ledgers);
        meta.put("gstRates", Arrays.asList(0, 0.1, 0.25, 1.5, 3, 5, 12, 18, 28));
        meta.put("gstOptions", Arrays.asList("Auto (by vendor state)", "Input CGST + SGST", "Input IGST", "No GST / Exempt"));
        meta.put("itcOptions", Arrays.asList(
                "Eligible",
                "Ineligible - Sec 17(5) blocked credit",
                "Ineligible - exempt / non-business use",
                "Ineligible - other"
        ));

        return ApiResponse.ok(meta);
    }

    @PostMapping
    @Transactional
    public ApiResponse<Map<String, Object>> createBill(@RequestBody PurchaseBillFormDTO form) {
        String tenantId = TenantContext.getTenantId();
        if (form.getPartyId() == null) {
            throw new IllegalArgumentException("Choose the vendor.");
        }
        if (form.getBillNo() == null || form.getBillNo().isBlank()) {
            throw new IllegalArgumentException("Bill No is required.");
        }
        if (form.getBillDate() == null || form.getBillDate().isBlank()) {
            throw new IllegalArgumentException("Bill Date is required.");
        }
        if (form.getLines() == null || form.getLines().isEmpty()) {
            throw new IllegalArgumentException("Add at least one line.");
        }

        // Count invoices for PI number
        Integer cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM invoices WHERE tenant_id = ? AND invoice_type = 'purchase'",
                Integer.class, tenantId
        );
        String invoiceNo = String.format("PI-%05d", (cnt != null ? cnt : 0) + 1);

        BigDecimal totalTaxable = BigDecimal.ZERO;
        BigDecimal totalCgst = BigDecimal.ZERO;
        BigDecimal totalSgst = BigDecimal.ZERO;
        BigDecimal totalIgst = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal rcmTax = BigDecimal.ZERO;

        for (PurchaseBillLineDTO l : form.getLines()) {
            BigDecimal qty = l.getQty() != null ? l.getQty() : BigDecimal.ONE;
            BigDecimal rate = l.getRate() != null ? l.getRate() : BigDecimal.ZERO;
            BigDecimal discount = l.getDiscount() != null ? l.getDiscount() : BigDecimal.ZERO;
            BigDecimal gross = qty.multiply(rate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal taxable = gross.subtract(discount).setScale(2, RoundingMode.HALF_UP);
            BigDecimal gstPct = l.getGstPct() != null ? l.getGstPct() : BigDecimal.valueOf(18);

            boolean isRcm = "Yes".equalsIgnoreCase(l.getRcm());
            boolean isImport = "Yes".equalsIgnoreCase(l.getIsImport());

            BigDecimal taxAmt = taxable.multiply(gstPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            if (isRcm || isImport) {
                rcmTax = rcmTax.add(taxAmt);
                totalTaxable = totalTaxable.add(taxable);
                totalAmount = totalAmount.add(taxable); // vendor gets only taxable
            } else {
                BigDecimal halfTax = taxAmt.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                totalCgst = totalCgst.add(halfTax);
                totalSgst = totalSgst.add(halfTax);
                totalTaxable = totalTaxable.add(taxable);
                totalAmount = totalAmount.add(taxable.add(taxAmt));
            }
        }

        String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Auto post voucher
        String vSql = "INSERT INTO vouchers (tenant_id, voucher_number, voucher_date, voucher_type, narration, total_amount, created_at) " +
                      "VALUES (?, ?, ?, 'PURCHASE', ?, ?, ?)";
        jdbcTemplate.update(vSql, tenantId, "PDB-" + invoiceNo, form.getBillDate(),
                "Purchase bill " + form.getBillNo() + ": " + (form.getNarration() != null ? form.getNarration() : ""),
                totalAmount, nowStr);
        Long voucherId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

        // Insert invoice
        String invSql = "INSERT INTO invoices (tenant_id, invoice_no, invoice_type, party_id, invoice_date, due_date, " +
                        "                      taxable_value, cgst, sgst, igst, total_amount, status, voucher_id, " +
                        "                      vendor_bill_no, rcm_tax, created_at) " +
                        "VALUES (?, ?, 'purchase', ?, ?, ?, ?, ?, ?, ?, ?, 'posted', ?, ?, ?, ?)";
        jdbcTemplate.update(invSql, tenantId, invoiceNo, form.getPartyId(), form.getBillDate(),
                form.getDueDate() != null && !form.getDueDate().isBlank() ? form.getDueDate() : form.getBillDate(),
                totalTaxable, totalCgst, totalSgst, totalIgst, totalAmount, voucherId,
                form.getBillNo(), rcmTax, nowStr);
        Long invoiceId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

        // Insert invoice lines
        String lineSql = "INSERT INTO invoice_lines (tenant_id, invoice_id, description, hsn_sac, qty, rate, " +
                         "                          taxable_value, gst_rate, cgst, sgst, igst, total_amount) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        for (PurchaseBillLineDTO l : form.getLines()) {
            BigDecimal qty = l.getQty() != null ? l.getQty() : BigDecimal.ONE;
            BigDecimal rate = l.getRate() != null ? l.getRate() : BigDecimal.ZERO;
            BigDecimal discount = l.getDiscount() != null ? l.getDiscount() : BigDecimal.ZERO;
            BigDecimal taxable = qty.multiply(rate).subtract(discount).setScale(2, RoundingMode.HALF_UP);
            BigDecimal gstPct = l.getGstPct() != null ? l.getGstPct() : BigDecimal.valueOf(18);
            BigDecimal taxAmt = taxable.multiply(gstPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal halfTax = taxAmt.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);

            jdbcTemplate.update(lineSql, tenantId, invoiceId, l.getDesc(), l.getHsn(), qty, rate,
                    taxable, gstPct, halfTax, halfTax, BigDecimal.ZERO, taxable.add(taxAmt));
        }

        Map<String, Object> res = new HashMap<>();
        res.put("invoiceId", invoiceId);
        res.put("invoiceNo", invoiceNo);
        res.put("total", totalAmount);
        return ApiResponse.ok("Purchase bill " + invoiceNo + " posted successfully.", res);
    }
}
