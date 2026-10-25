package com.finvane.controller.financialmodel;

import com.finvane.model.common.ApiResponse;
import com.finvane.model.common.TenantContext;
import com.finvane.model.financialmodel.FinancialModelDTO;
import com.finvane.model.financialmodel.FinancialModelDTO.*;
import com.finvane.utils.SectorBenchmarks;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/v2/financial-model")
@RequiredArgsConstructor
public class FinancialModelController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public ApiResponse<CorporateFinanceDataDTO> getCorporateFinanceData(
            @RequestParam(required = false, defaultValue = "3") Integer years,
            @RequestParam(required = false) String sector) {

        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";

        int numYears = Math.max(1, Math.min(5, years != null ? years : 3));
        String sectorKey = (sector != null && !sector.isBlank()) ? sector.trim() : null;
        SectorBenchmarkDTO sectorBenchmark = SectorBenchmarks.findSector(sectorKey);

        List<Integer> startYears = listHistoricalFYStartYears(tenantId);
        List<FinancialYearDTO> historicalFYs = new ArrayList<>();
        for (int y : startYears) {
            FinancialYearDTO fy = computeHistoricalFY(tenantId, y);
            if (fy != null) {
                historicalFYs.add(fy);
            }
        }

        if (historicalFYs.isEmpty()) {
            FinancialModelDTO emptyModel = FinancialModelDTO.builder()
                    .hasData(false)
                    .numYears(numYears)
                    .sector(sectorBenchmark)
                    .historicalFYs(Collections.emptyList())
                    .projectedFYs(Collections.emptyList())
                    .assumptions(AssumptionsDTO.builder().note("No posted financial history yet.").build())
                    .build();
            ValuationReportDTO emptyValuation = ValuationReportDTO.builder().hasData(false).build();
            CmaReportDTO emptyCma = CmaReportDTO.builder().hasData(false).build();

            return ApiResponse.ok(CorporateFinanceDataDTO.builder()
                    .model(emptyModel)
                    .valuation(emptyValuation)
                    .cma(emptyCma)
                    .years(numYears)
                    .sectorKey(sectorKey)
                    .sectors(SectorBenchmarks.listSectors())
                    .build());
        }

        FinancialYearDTO latestFY = historicalFYs.get(historicalFYs.size() - 1);
        ProjectionResult projection = projectForward(latestFY, historicalFYs, numYears, sectorBenchmark);
        SegmentsDTO segments = computeSegments(tenantId, latestFY.getFrom(), latestFY.getTo());
        UnitEconomicsDTO unitEconomics = computeUnitEconomics(tenantId, latestFY.getFrom(), latestFY.getTo(), latestFY.getRevenue());
        BenchmarkComparisonDTO benchmarkComparison = compareToBenchmark(latestFY, sectorBenchmark);

        String note = (sectorBenchmark != null
                ? "Growth and margin targets are laid over your actual " + latestFY.getLabel() + " base using the " + sectorBenchmark.getLabel() + " sector's typical ranges -- booked historicals are never altered."
                : "No sector selected -- projection continues at your own historical revenue CAGR and last actual margin."
        ) + " Historical Net Profit figures are read directly from the P&L (no tax deducted, matching the books); the assumed 25% tax rate is applied only to the forward-looking projected years.";

        FinancialModelDTO modelDTO = FinancialModelDTO.builder()
                .hasData(true)
                .numYears(numYears)
                .sector(sectorBenchmark)
                .historicalFYs(historicalFYs)
                .projectedFYs(projection.rows)
                .growthRateUsedPct(projection.growthRateUsedPct)
                .usedSectorGrowth(projection.usedSectorGrowth)
                .segments(segments)
                .unitEconomics(unitEconomics)
                .benchmarkComparison(benchmarkComparison)
                .assumptions(AssumptionsDTO.builder()
                        .assumedTaxRatePct(25)
                        .assumedTaxRateAppliesToProjectionOnly(true)
                        .fallbackGrowthPct(projection.fallbackGrowthPct)
                        .note(note)
                        .build())
                .build();

        ValuationReportDTO valuationDTO = buildValuationReport(modelDTO, sectorBenchmark);
        CmaReportDTO cmaDTO = buildCmaReport(modelDTO);

        CorporateFinanceDataDTO data = CorporateFinanceDataDTO.builder()
                .model(modelDTO)
                .valuation(valuationDTO)
                .cma(cmaDTO)
                .years(numYears)
                .sectorKey(sectorKey)
                .sectors(SectorBenchmarks.listSectors())
                .build();

        return ApiResponse.ok(data);
    }

    @GetMapping("/segment-breakdown")
    public ApiResponse<Map<String, Object>> getSegmentBreakdown(
            @RequestParam(required = false, defaultValue = "3") Integer years,
            @RequestParam(required = false) String sector,
            @RequestParam(required = false, defaultValue = "") String segment) {

        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";

        List<Integer> startYears = listHistoricalFYStartYears(tenantId);
        if (startYears.isEmpty()) {
            return ApiResponse.ok(Map.of("segment", segment, "rows", Collections.emptyList()));
        }
        int latestStartYear = startYears.get(startYears.size() - 1);
        String from = latestStartYear + "-04-01";
        String to = (latestStartYear + 1) + "-03-31";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT it.name as itemName, it.sku, SUM(il.qty) as qty, SUM(il.amount) as revenue, COUNT(DISTINCT i.id) as invoiceCount " +
                "FROM invoice_lines il " +
                "JOIN invoices i ON i.id = il.invoice_id " +
                "JOIN items it ON it.id = il.item_id " +
                "WHERE i.tenant_id = ? AND i.invoice_type = 'sales' AND i.status = 'posted' AND i.invoice_date BETWEEN ? AND ? " +
                "  AND COALESCE(NULLIF(TRIM(it.category), ''), 'Uncategorized') = ? " +
                "GROUP BY it.id, it.name, it.sku ORDER BY revenue DESC",
                tenantId, from, to, segment
        );

        return ApiResponse.ok(Map.of(
                "segment", segment,
                "from", from,
                "to", to,
                "rows", rows
        ));
    }

    // -----------------------------------------------------------------------------------------
    // Helper Methods for Financial Model Computation
    // -----------------------------------------------------------------------------------------

    private List<Integer> listHistoricalFYStartYears(String tenantId) {
        try {
            Map<String, Object> minMax = jdbcTemplate.queryForMap(
                    "SELECT MIN(voucher_date) as minD, MAX(voucher_date) as maxD FROM vouchers WHERE tenant_id = ?",
                    tenantId
            );
            Object minD = minMax.get("minD");
            Object maxD = minMax.get("maxD");
            if (minD == null || maxD == null) {
                if ("2".equals(tenantId)) return List.of(2026);
                return List.of(2024, 2025, 2026);
            }
            int startFY = fyStartYearOf(minD.toString());
            int endFY = fyStartYearOf(maxD.toString());
            List<Integer> list = new ArrayList<>();
            for (int y = startFY; y <= endFY; y++) {
                list.add(y);
            }
            return list;
        } catch (Exception e) {
            if ("2".equals(tenantId)) return List.of(2026);
            return List.of(2024, 2025, 2026);
        }
    }

    private int fyStartYearOf(String dateStr) {
        String[] parts = dateStr.split("-");
        int y = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        return m >= 4 ? y : y - 1;
    }

    private String fyLabel(int startYear) {
        return "FY" + String.valueOf(startYear).substring(2) + "-" + String.valueOf(startYear + 1).substring(2);
    }

    private FinancialYearDTO computeHistoricalFY(String tenantId, int startYear) {
        String from = startYear + "-04-01";
        String to = (startYear + 1) + "-03-31";
        String label = fyLabel(startYear);

        BigDecimal revenue = queryBigDecimal(
                "SELECT COALESCE(SUM(CASE WHEN vi.entry_type='CREDIT' THEN vi.amount ELSE -vi.amount END), 0) " +
                "FROM voucher_items vi " +
                "JOIN vouchers v ON v.id = vi.voucher_id " +
                "JOIN account_ledgers l ON l.id = vi.ledger_id " +
                "JOIN account_groups g ON g.id = l.group_id " +
                "WHERE v.tenant_id = ? AND v.voucher_date BETWEEN ? AND ? " +
                "  AND (g.id IN (10, 12, 58, 59, 60) OR g.parent_id IN (10, 12))",
                tenantId, from, to
        );

        BigDecimal tradingExpense = queryBigDecimal(
                "SELECT COALESCE(SUM(CASE WHEN vi.entry_type='DEBIT' THEN vi.amount ELSE -vi.amount END), 0) " +
                "FROM voucher_items vi " +
                "JOIN vouchers v ON v.id = vi.voucher_id " +
                "JOIN account_ledgers l ON l.id = vi.ledger_id " +
                "JOIN account_groups g ON g.id = l.group_id " +
                "WHERE v.tenant_id = ? AND v.voucher_date BETWEEN ? AND ? " +
                "  AND (g.id IN (11, 14, 61, 62, 63, 64, 65, 66) OR g.parent_id IN (11, 14))",
                tenantId, from, to
        );

        boolean inventoryEnabled = !"2".equals(tenantId);
        BigDecimal openingStock = inventoryEnabled ? stockValueAsOf(tenantId, dayBefore(from)) : BigDecimal.ZERO;
        BigDecimal closingStock = inventoryEnabled ? stockValueAsOf(tenantId, to) : BigDecimal.ZERO;

        BigDecimal cogs = tradingExpense.add(openingStock).subtract(closingStock).setScale(2, RoundingMode.HALF_UP);
        BigDecimal grossProfit = revenue.add(closingStock).subtract(tradingExpense).subtract(openingStock).setScale(2, RoundingMode.HALF_UP);

        BigDecimal depreciation = queryBigDecimal(
                "SELECT COALESCE(SUM(CASE WHEN vi.entry_type='DEBIT' THEN vi.amount ELSE -vi.amount END), 0) " +
                "FROM voucher_items vi " +
                "JOIN vouchers v ON v.id = vi.voucher_id " +
                "JOIN account_ledgers l ON l.id = vi.ledger_id " +
                "WHERE v.tenant_id = ? AND v.voucher_date BETWEEN ? AND ? AND l.code = '5900'",
                tenantId, from, to
        );

        BigDecimal indirectExpenseTotalAll = queryBigDecimal(
                "SELECT COALESCE(SUM(CASE WHEN vi.entry_type='DEBIT' THEN vi.amount ELSE -vi.amount END), 0) " +
                "FROM voucher_items vi " +
                "JOIN vouchers v ON v.id = vi.voucher_id " +
                "JOIN account_ledgers l ON l.id = vi.ledger_id " +
                "JOIN account_groups g ON g.id = l.group_id " +
                "WHERE v.tenant_id = ? AND v.voucher_date BETWEEN ? AND ? " +
                "  AND (g.id = 15 OR g.parent_id = 15)",
                tenantId, from, to
        );

        BigDecimal indirectExpenseTotal = indirectExpenseTotalAll.subtract(depreciation).setScale(2, RoundingMode.HALF_UP);

        BigDecimal indirectIncomeTotal = queryBigDecimal(
                "SELECT COALESCE(SUM(CASE WHEN vi.entry_type='CREDIT' THEN vi.amount ELSE -vi.amount END), 0) " +
                "FROM voucher_items vi " +
                "JOIN vouchers v ON v.id = vi.voucher_id " +
                "JOIN account_ledgers l ON l.id = vi.ledger_id " +
                "JOIN account_groups g ON g.id = l.group_id " +
                "WHERE v.tenant_id = ? AND v.voucher_date BETWEEN ? AND ? " +
                "  AND (g.id = 13 OR g.parent_id = 13)",
                tenantId, from, to
        );

        BigDecimal ebitda = grossProfit.add(indirectIncomeTotal).subtract(indirectExpenseTotal).setScale(2, RoundingMode.HALF_UP);
        BigDecimal ebit = ebitda.subtract(depreciation).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal netProfit = ebit;

        // Balance sheet figures as of to
        BigDecimal debtorsBal = queryBalanceByGroupName(tenantId, "Sundry Debtors", to);
        BigDecimal creditorsBal = queryBalanceByGroupName(tenantId, "Sundry Creditors", to).abs();
        BigDecimal inventoryBal = closingStock;

        BigDecimal currentAssetsWithoutStock = queryBalanceByPrimaryGroups(tenantId, List.of("Current Assets"), to);
        BigDecimal currentAssets = currentAssetsWithoutStock.add(inventoryBal).setScale(2, RoundingMode.HALF_UP);
        BigDecimal currentLiabilities = queryBalanceByPrimaryGroups(tenantId, List.of("Current Liabilities"), to).abs().setScale(2, RoundingMode.HALF_UP);
        BigDecimal fixedAssets = queryBalanceByPrimaryGroups(tenantId, List.of("Fixed Assets"), to);
        BigDecimal investments = queryBalanceByPrimaryGroups(tenantId, List.of("Investments"), to);
        BigDecimal otherAssets = queryBalanceByPrimaryGroups(tenantId, List.of("Branch / Divisions", "Misc. Expenses (Asset)", "Suspense A/c"), to);
        BigDecimal totalAssets = currentAssets.add(fixedAssets).add(investments).add(otherAssets).setScale(2, RoundingMode.HALF_UP);

        BigDecimal equity = queryBalanceByPrimaryGroups(tenantId, List.of("Capital Account"), to).abs().setScale(2, RoundingMode.HALF_UP);
        BigDecimal debt = queryBalanceByPrimaryGroups(tenantId, List.of("Loans (Liability)"), to).abs().setScale(2, RoundingMode.HALF_UP);

        int salesCount = 0;
        BigDecimal purchaseTotal = BigDecimal.ZERO;
        try {
            salesCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM invoices WHERE tenant_id = ? AND invoice_type = 'sales' AND status = 'posted' AND invoice_date BETWEEN ? AND ?",
                    Integer.class, tenantId, from, to
            );
            purchaseTotal = queryBigDecimal(
                    "SELECT COALESCE(SUM(taxable_value), 0) FROM invoices WHERE tenant_id = ? AND invoice_type = 'purchase' AND status = 'posted' AND invoice_date BETWEEN ? AND ?",
                    tenantId, from, to
            );
        } catch (Exception ignored) {}

        long periodDays = Math.max(1, ChronoUnit.DAYS.between(LocalDate.parse(from), LocalDate.parse(to)) + 1);

        Double dso = revenue.compareTo(BigDecimal.ZERO) > 0 ? round2(debtorsBal.multiply(BigDecimal.valueOf(periodDays)).divide(revenue, 4, RoundingMode.HALF_UP).doubleValue()) : null;
        Double dpo = purchaseTotal.compareTo(BigDecimal.ZERO) > 0 ? round2(creditorsBal.multiply(BigDecimal.valueOf(periodDays)).divide(purchaseTotal, 4, RoundingMode.HALF_UP).doubleValue()) : null;
        Double dio = cogs.compareTo(BigDecimal.ZERO) > 0 ? round2(inventoryBal.multiply(BigDecimal.valueOf(periodDays)).divide(cogs, 4, RoundingMode.HALF_UP).doubleValue()) : null;
        Double c2c = (dso != null && dpo != null && dio != null) ? round2(dso + dio - dpo) : null;

        FinancialRatiosDTO ratios = FinancialRatiosDTO.builder()
                .grossMarginPct(pct(grossProfit, revenue))
                .ebitdaMarginPct(pct(ebitda, revenue))
                .netMarginPct(pct(netProfit, revenue))
                .currentRatio(currentLiabilities.compareTo(BigDecimal.ZERO) > 0 ? round2(currentAssets.divide(currentLiabilities, 4, RoundingMode.HALF_UP).doubleValue()) : null)
                .quickRatio(currentLiabilities.compareTo(BigDecimal.ZERO) > 0 ? round2(currentAssets.subtract(inventoryBal).divide(currentLiabilities, 4, RoundingMode.HALF_UP).doubleValue()) : null)
                .debtEquity(equity.compareTo(BigDecimal.ZERO) > 0 ? round2(debt.divide(equity, 4, RoundingMode.HALF_UP).doubleValue()) : null)
                .roe(equity.compareTo(BigDecimal.ZERO) > 0 ? pct(netProfit, equity) : null)
                .roce((equity.add(debt)).compareTo(BigDecimal.ZERO) > 0 ? pct(ebit, equity.add(debt)) : null)
                .assetTurnover(totalAssets.compareTo(BigDecimal.ZERO) > 0 ? round2(revenue.divide(totalAssets, 4, RoundingMode.HALF_UP).doubleValue()) : null)
                .dso(dso)
                .dpo(dpo)
                .dio(dio)
                .c2c(c2c)
                .build();

        return FinancialYearDTO.builder()
                .startYear(startYear)
                .label(label)
                .from(from)
                .to(to)
                .revenue(revenue)
                .cogs(cogs)
                .grossProfit(grossProfit)
                .indirectIncomeTotal(indirectIncomeTotal)
                .indirectExpenseTotal(indirectExpenseTotal)
                .depreciation(depreciation)
                .ebitda(ebitda)
                .ebit(ebit)
                .tax(tax)
                .netProfit(netProfit)
                .totalAssets(totalAssets)
                .equity(equity)
                .debt(debt)
                .currentAssets(currentAssets)
                .currentLiabilities(currentLiabilities)
                .salesInvoiceCount(salesCount)
                .purchaseTotal(purchaseTotal)
                .isProjected(false)
                .ratios(ratios)
                .build();
    }

    private static class ProjectionResult {
        List<FinancialYearDTO> rows;
        BigDecimal growthRateUsedPct;
        boolean usedSectorGrowth;
        double fallbackGrowthPct;
    }

    private ProjectionResult projectForward(FinancialYearDTO baseFY, List<FinancialYearDTO> historicalFYs, int years, SectorBenchmarkDTO sector) {
        double fallbackGrowth = blendedHistoricalGrowth(historicalFYs);
        double growthRate = (sector != null) ? (sector.getRevenueGrowth() / 100.0) : fallbackGrowth;

        double baseGrossMargin = baseFY.getRatios().getGrossMarginPct() != null ? baseFY.getRatios().getGrossMarginPct() : 0.0;
        double baseEbitdaMargin = baseFY.getRatios().getEbitdaMarginPct() != null ? baseFY.getRatios().getEbitdaMarginPct() : 0.0;
        double targetGrossMargin = (sector != null) ? sector.getGrossMargin().getMid() : baseGrossMargin;
        double targetEbitdaMargin = (sector != null) ? sector.getEbitdaMargin().getMid() : baseEbitdaMargin;

        List<FinancialYearDTO> projectedRows = new ArrayList<>();
        BigDecimal prevRevenue = baseFY.getRevenue();
        BigDecimal prevDep = baseFY.getDepreciation();
        BigDecimal assumedTaxRate = BigDecimal.valueOf(0.25);

        for (int i = 1; i <= years; i++) {
            double t = (double) i / years;
            double grossMarginPct = round2(baseGrossMargin + (targetGrossMargin - baseGrossMargin) * t);
            double ebitdaMarginPct = round2(baseEbitdaMargin + (targetEbitdaMargin - baseEbitdaMargin) * t);

            BigDecimal revenue = prevRevenue.multiply(BigDecimal.valueOf(1.0 + growthRate)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal grossProfit = revenue.multiply(BigDecimal.valueOf(grossMarginPct / 100.0)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal ebitda = revenue.multiply(BigDecimal.valueOf(ebitdaMarginPct / 100.0)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal depreciation = prevDep.multiply(BigDecimal.valueOf(1.0 + growthRate * 0.5)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal ebit = ebitda.subtract(depreciation).setScale(2, RoundingMode.HALF_UP);
            BigDecimal tax = ebit.compareTo(BigDecimal.ZERO) > 0 ? ebit.multiply(assumedTaxRate).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            BigDecimal netProfit = ebit.subtract(tax).setScale(2, RoundingMode.HALF_UP);

            FinancialRatiosDTO projRatios = FinancialRatiosDTO.builder()
                    .grossMarginPct(grossMarginPct)
                    .ebitdaMarginPct(ebitdaMarginPct)
                    .netMarginPct(pct(netProfit, revenue))
                    .build();

            projectedRows.add(FinancialYearDTO.builder()
                    .startYear(baseFY.getStartYear() + i)
                    .label(fyLabel(baseFY.getStartYear() + i) + " (P)")
                    .isProjected(true)
                    .revenue(revenue)
                    .grossProfit(grossProfit)
                    .ebitda(ebitda)
                    .depreciation(depreciation)
                    .ebit(ebit)
                    .tax(tax)
                    .netProfit(netProfit)
                    .ratios(projRatios)
                    .build());

            prevRevenue = revenue;
            prevDep = depreciation;
        }

        ProjectionResult res = new ProjectionResult();
        res.rows = projectedRows;
        res.growthRateUsedPct = BigDecimal.valueOf(growthRate * 100.0).setScale(2, RoundingMode.HALF_UP);
        res.usedSectorGrowth = (sector != null);
        res.fallbackGrowthPct = round2(fallbackGrowth * 100.0);
        return res;
    }

    private double blendedHistoricalGrowth(List<FinancialYearDTO> historicalFYs) {
        List<FinancialYearDTO> withRev = historicalFYs.stream().filter(f -> f.getRevenue().compareTo(BigDecimal.ZERO) > 0).toList();
        if (withRev.size() < 2) return 0.12;
        FinancialYearDTO first = withRev.get(0);
        FinancialYearDTO last = withRev.get(withRev.size() - 1);
        int years = last.getStartYear() - first.getStartYear();
        if (years <= 0 || first.getRevenue().compareTo(BigDecimal.ZERO) <= 0) return 0.12;
        double ratio = last.getRevenue().doubleValue() / first.getRevenue().doubleValue();
        double cagr = Math.pow(ratio, 1.0 / years) - 1.0;
        return Math.max(-0.30, Math.min(0.60, cagr));
    }

    private SegmentsDTO computeSegments(String tenantId, String from, String to) {
        List<CategorySegmentDTO> byCategory = new ArrayList<>();
        List<LocationSegmentDTO> byLocation = new ArrayList<>();
        if ("2".equals(tenantId)) {
            return SegmentsDTO.builder().byCategory(byCategory).byLocation(byLocation).build();
        }

        try {
            List<Map<String, Object>> catRows = jdbcTemplate.queryForList(
                    "SELECT COALESCE(NULLIF(TRIM(it.category), ''), 'Uncategorized') AS segment, " +
                    "  COALESCE(SUM(il.taxable_value), 0) AS revenue, COALESCE(SUM(il.qty), 0) AS qty " +
                    "FROM invoice_lines il " +
                    "JOIN invoices i ON i.id = il.invoice_id " +
                    "JOIN items it ON it.id = il.item_id " +
                    "WHERE i.tenant_id = ? AND i.invoice_type = 'sales' AND i.status = 'posted' AND i.invoice_date BETWEEN ? AND ? " +
                    "GROUP BY segment ORDER BY revenue DESC",
                    tenantId, from, to
            );
            BigDecimal totalCatRev = BigDecimal.ZERO;
            for (Map<String, Object> r : catRows) {
                Number rev = (Number) r.get("revenue");
                if (rev != null) totalCatRev = totalCatRev.add(BigDecimal.valueOf(rev.doubleValue()));
            }
            for (Map<String, Object> r : catRows) {
                String seg = (String) r.get("segment");
                Number rev = (Number) r.get("revenue");
                Number qty = (Number) r.get("qty");
                BigDecimal bRev = rev != null ? BigDecimal.valueOf(rev.doubleValue()).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                Long lQty = qty != null ? qty.longValue() : 0L;
                Double share = totalCatRev.compareTo(BigDecimal.ZERO) > 0 ? round2(bRev.doubleValue() * 100.0 / totalCatRev.doubleValue()) : 0.0;
                byCategory.add(new CategorySegmentDTO(seg, bRev, lQty, share));
            }

            List<Map<String, Object>> locRows = jdbcTemplate.queryForList(
                    "SELECT COALESCE(l.name, 'Unassigned') AS segment, " +
                    "  COALESCE(SUM(i.taxable_value), 0) AS revenue, COUNT(*) AS invoiceCount " +
                    "FROM invoices i " +
                    "LEFT JOIN locations l ON l.id = i.location_id " +
                    "WHERE i.tenant_id = ? AND i.invoice_type = 'sales' AND i.status = 'posted' AND i.invoice_date BETWEEN ? AND ? " +
                    "GROUP BY segment ORDER BY revenue DESC",
                    tenantId, from, to
            );
            BigDecimal totalLocRev = totalCatRev;
            for (Map<String, Object> r : locRows) {
                String seg = (String) r.get("segment");
                Number rev = (Number) r.get("revenue");
                Number cnt = (Number) r.get("invoiceCount");
                BigDecimal bRev = rev != null ? BigDecimal.valueOf(rev.doubleValue()).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                Long lCnt = cnt != null ? cnt.longValue() : 0L;
                Double share = totalLocRev.compareTo(BigDecimal.ZERO) > 0 ? round2(bRev.doubleValue() * 100.0 / totalLocRev.doubleValue()) : 0.0;
                byLocation.add(new LocationSegmentDTO(seg, bRev, lCnt, share));
            }
        } catch (Exception e) {
            log.warn("Error computing segments: {}", e.getMessage());
        }

        return SegmentsDTO.builder().byCategory(byCategory).byLocation(byLocation).build();
    }

    private UnitEconomicsDTO computeUnitEconomics(String tenantId, String from, String to, BigDecimal revenue) {
        int salesCount = 0;
        int distinctCustomers = 0;
        long unitsSold = 0L;
        BigDecimal cogs = BigDecimal.ZERO;
        long activeHeadcount = 0L;
        BigDecimal payrollCost = BigDecimal.ZERO;

        try {
            salesCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM invoices WHERE tenant_id = ? AND invoice_type = 'sales' AND status = 'posted' AND invoice_date BETWEEN ? AND ?",
                    Integer.class, tenantId, from, to
            );
            distinctCustomers = jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT party_id) FROM invoices WHERE tenant_id = ? AND invoice_type = 'sales' AND status = 'posted' AND invoice_date BETWEEN ? AND ?",
                    Integer.class, tenantId, from, to
            );
            unitsSold = queryLong(
                    "SELECT COALESCE(SUM(il.qty), 0) FROM invoice_lines il JOIN invoices i ON i.id = il.invoice_id " +
                    "WHERE i.tenant_id = ? AND i.invoice_type = 'sales' AND i.status = 'posted' AND i.invoice_date BETWEEN ? AND ?",
                    tenantId, from, to
            );
            cogs = queryBigDecimal(
                    "SELECT COALESCE(SUM(consumed_qty * purchase_rate), 0) FROM stock_lot_consumptions WHERE tenant_id = ? AND sale_ref_type = 'sale' AND sale_date BETWEEN ? AND ?",
                    tenantId, from, to
            );
            activeHeadcount = queryLong(
                    "SELECT COUNT(*) FROM employees WHERE tenant_id = ? AND date_of_joining IS NOT NULL AND date_of_joining <= ? AND (date_of_exit IS NULL OR date_of_exit > ?)",
                    tenantId, to, to
            );
            payrollCost = queryBigDecimal(
                    "SELECT COALESCE(SUM(ps.gross), 0) FROM payslips ps " +
                    "JOIN payroll_runs pr ON pr.id = ps.payroll_run_id " +
                    "WHERE pr.tenant_id = ? AND pr.status = 'processed' " +
                    "  AND CONCAT(pr.period_year, '-', LPAD(pr.period_month, 2, '0'), '-01') BETWEEN ? AND ?",
                    tenantId, from.substring(0, 7) + "-01", to.substring(0, 7) + "-01"
            );
        } catch (Exception e) {
            log.warn("Error computing unit economics: {}", e.getMessage());
        }

        BigDecimal aov = salesCount > 0 ? revenue.divide(BigDecimal.valueOf(salesCount), 2, RoundingMode.HALF_UP) : null;
        BigDecimal revPerCust = distinctCustomers > 0 ? revenue.divide(BigDecimal.valueOf(distinctCustomers), 2, RoundingMode.HALF_UP) : null;
        BigDecimal costPerUnit = unitsSold > 0 ? cogs.divide(BigDecimal.valueOf(unitsSold), 2, RoundingMode.HALF_UP) : null;
        BigDecimal revPerUnit = unitsSold > 0 ? revenue.divide(BigDecimal.valueOf(unitsSold), 2, RoundingMode.HALF_UP) : null;
        BigDecimal grossMarginPerUnit = unitsSold > 0 ? revenue.subtract(cogs).divide(BigDecimal.valueOf(unitsSold), 2, RoundingMode.HALF_UP) : null;
        BigDecimal revPerEmp = activeHeadcount > 0 ? revenue.divide(BigDecimal.valueOf(activeHeadcount), 2, RoundingMode.HALF_UP) : null;
        Double payrollPct = revenue.compareTo(BigDecimal.ZERO) > 0 ? round2(payrollCost.doubleValue() * 100.0 / revenue.doubleValue()) : null;

        return UnitEconomicsDTO.builder()
                .averageOrderValue(aov)
                .revenuePerCustomer(revPerCust)
                .distinctCustomers(distinctCustomers)
                .salesInvoiceCount(salesCount)
                .unitsSold(unitsSold)
                .costPerUnit(costPerUnit)
                .revenuePerUnit(revPerUnit)
                .grossMarginPerUnit(grossMarginPerUnit)
                .activeHeadcount(activeHeadcount)
                .revenuePerEmployee(revPerEmp)
                .payrollCostForPeriod(payrollCost)
                .payrollCostPctOfRevenue(payrollPct)
                .build();
    }

    private BenchmarkComparisonDTO compareToBenchmark(FinancialYearDTO latestFY, SectorBenchmarkDTO sector) {
        if (sector == null) return null;

        FinancialRatiosDTO r = latestFY.getRatios();
        return BenchmarkComparisonDTO.builder()
                .sectorLabel(sector.getLabel())
                .grossMargin(flag(r.getGrossMarginPct(), sector.getGrossMargin()))
                .ebitdaMargin(flag(r.getEbitdaMarginPct(), sector.getEbitdaMargin()))
                .netMargin(flag(r.getNetMarginPct(), sector.getNetMargin()))
                .dso(flag(r.getDso(), RangeVal.builder().low(0.0).mid((double) sector.getDso()).high((double) sector.getDso()).build()))
                .dio(flag(r.getDio(), RangeVal.builder().low(0.0).mid((double) sector.getDio()).high((double) sector.getDio()).build()))
                .unitEconomicsNote(sector.getUnitEconomicsNote())
                .build();
    }

    private BenchmarkFlagDTO flag(Double value, RangeVal band) {
        if (value == null) return new BenchmarkFlagDTO(null, "no_data", band);
        String status = "within";
        if (value < band.getLow()) status = "below";
        else if (value > band.getHigh()) status = "above";
        return new BenchmarkFlagDTO(value, status, band);
    }

    private ValuationReportDTO buildValuationReport(FinancialModelDTO model, SectorBenchmarkDTO sector) {
        if (!model.isHasData() || model.getHistoricalFYs().isEmpty()) {
            return ValuationReportDTO.builder().hasData(false).build();
        }

        FinancialYearDTO latestFY = model.getHistoricalFYs().get(model.getHistoricalFYs().size() - 1);
        BigDecimal netDebt = latestFY.getDebt() != null ? latestFY.getDebt() : BigDecimal.ZERO;

        // 1. DCF
        DcfValuationDTO dcf = null;
        if (!model.getProjectedFYs().isEmpty()) {
            double wacc = 0.14;
            double g = 0.04;
            List<DcfRowDTO> rows = new ArrayList<>();
            BigDecimal sumPvFcf = BigDecimal.ZERO;
            BigDecimal lastFcf = BigDecimal.ZERO;
            double lastDf = 1.0;

            for (int i = 0; i < model.getProjectedFYs().size(); i++) {
                FinancialYearDTO pf = model.getProjectedFYs().get(i);
                BigDecimal fcf = pf.getNetProfit().add(pf.getDepreciation()).setScale(2, RoundingMode.HALF_UP);
                double df = round2(1.0 / Math.pow(1.0 + wacc, i + 1));
                BigDecimal pvFcf = fcf.multiply(BigDecimal.valueOf(df)).setScale(2, RoundingMode.HALF_UP);
                rows.add(new DcfRowDTO(pf.getLabel(), fcf, df, pvFcf));
                sumPvFcf = sumPvFcf.add(pvFcf);
                lastFcf = fcf;
                lastDf = df;
            }

            BigDecimal terminalValue = (wacc > g)
                    ? lastFcf.multiply(BigDecimal.valueOf(1.0 + g)).divide(BigDecimal.valueOf(wacc - g), 2, RoundingMode.HALF_UP)
                    : null;
            BigDecimal pvTerminalValue = terminalValue != null ? terminalValue.multiply(BigDecimal.valueOf(lastDf)).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            BigDecimal enterpriseValue = sumPvFcf.add(pvTerminalValue).setScale(2, RoundingMode.HALF_UP);
            BigDecimal equityValue = enterpriseValue.subtract(netDebt).setScale(2, RoundingMode.HALF_UP);

            dcf = DcfValuationDTO.builder()
                    .wacc(round2(wacc * 100.0))
                    .terminalGrowth(round2(g * 100.0))
                    .rows(rows)
                    .terminalValue(terminalValue)
                    .pvTerminalValue(pvTerminalValue)
                    .sumPvFcf(sumPvFcf)
                    .enterpriseValue(enterpriseValue)
                    .netDebt(netDebt)
                    .equityValue(equityValue)
                    .build();
        }

        // 2. Comparables
        RangeVal evEbitda = (sector != null) ? sector.getEvEbitdaMultiple() : RangeVal.builder().low(8.0).mid(8.0).high(8.0).build();
        RangeVal pe = (sector != null) ? sector.getPeRatio() : RangeVal.builder().low(15.0).mid(15.0).high(15.0).build();

        ValuationRangeDTO evEbitdaVal = ValuationRangeDTO.builder()
                .low(latestFY.getEbitda().multiply(BigDecimal.valueOf(evEbitda.getLow())).setScale(2, RoundingMode.HALF_UP))
                .mid(latestFY.getEbitda().multiply(BigDecimal.valueOf(evEbitda.getMid())).setScale(2, RoundingMode.HALF_UP))
                .high(latestFY.getEbitda().multiply(BigDecimal.valueOf(evEbitda.getHigh())).setScale(2, RoundingMode.HALF_UP))
                .build();
        ValuationRangeDTO eqEvEbitda = ValuationRangeDTO.builder()
                .low(evEbitdaVal.getLow().subtract(netDebt))
                .mid(evEbitdaVal.getMid().subtract(netDebt))
                .high(evEbitdaVal.getHigh().subtract(netDebt))
                .build();

        ValuationRangeDTO peVal = ValuationRangeDTO.builder()
                .low(latestFY.getNetProfit().multiply(BigDecimal.valueOf(pe.getLow())).setScale(2, RoundingMode.HALF_UP))
                .mid(latestFY.getNetProfit().multiply(BigDecimal.valueOf(pe.getMid())).setScale(2, RoundingMode.HALF_UP))
                .high(latestFY.getNetProfit().multiply(BigDecimal.valueOf(pe.getHigh())).setScale(2, RoundingMode.HALF_UP))
                .build();

        double ebitdaFrac = latestFY.getRatios().getEbitdaMarginPct() != null ? latestFY.getRatios().getEbitdaMarginPct() / 100.0 : 0.0;
        RangeVal revMult = RangeVal.builder()
                .low(round2(evEbitda.getLow() * ebitdaFrac))
                .mid(round2(evEbitda.getMid() * ebitdaFrac))
                .high(round2(evEbitda.getHigh() * ebitdaFrac))
                .build();
        ValuationRangeDTO evRevVal = ValuationRangeDTO.builder()
                .low(latestFY.getRevenue().multiply(BigDecimal.valueOf(revMult.getLow())).setScale(2, RoundingMode.HALF_UP))
                .mid(latestFY.getRevenue().multiply(BigDecimal.valueOf(revMult.getMid())).setScale(2, RoundingMode.HALF_UP))
                .high(latestFY.getRevenue().multiply(BigDecimal.valueOf(revMult.getHigh())).setScale(2, RoundingMode.HALF_UP))
                .build();
        ValuationRangeDTO eqRev = ValuationRangeDTO.builder()
                .low(evRevVal.getLow().subtract(netDebt))
                .mid(evRevVal.getMid().subtract(netDebt))
                .high(evRevVal.getHigh().subtract(netDebt))
                .build();

        double controlPremium = 0.25;
        RangeVal txMult = RangeVal.builder()
                .low(round2(evEbitda.getLow() * (1.0 + controlPremium)))
                .mid(round2(evEbitda.getMid() * (1.0 + controlPremium)))
                .high(round2(evEbitda.getHigh() * (1.0 + controlPremium)))
                .build();
        ValuationRangeDTO evTxVal = ValuationRangeDTO.builder()
                .low(latestFY.getEbitda().multiply(BigDecimal.valueOf(txMult.getLow())).setScale(2, RoundingMode.HALF_UP))
                .mid(latestFY.getEbitda().multiply(BigDecimal.valueOf(txMult.getMid())).setScale(2, RoundingMode.HALF_UP))
                .high(latestFY.getEbitda().multiply(BigDecimal.valueOf(txMult.getHigh())).setScale(2, RoundingMode.HALF_UP))
                .build();
        ValuationRangeDTO eqTx = ValuationRangeDTO.builder()
                .low(evTxVal.getLow().subtract(netDebt))
                .mid(evTxVal.getMid().subtract(netDebt))
                .high(evTxVal.getHigh().subtract(netDebt))
                .build();

        ComparablesValuationDTO comparables = ComparablesValuationDTO.builder()
                .basisFY(latestFY.getLabel())
                .ebitda(latestFY.getEbitda())
                .netProfit(latestFY.getNetProfit())
                .revenue(latestFY.getRevenue())
                .evEbitdaMultiple(evEbitda)
                .evEbitdaValuation(evEbitdaVal)
                .equityFromEvEbitda(eqEvEbitda)
                .peRatio(pe)
                .peValuation(peVal)
                .revenueMultiple(revMult)
                .evRevenueValuation(evRevVal)
                .equityFromRevenue(eqRev)
                .controlPremiumPct(25.0)
                .transactionMultiple(txMult)
                .evTransactionValuation(evTxVal)
                .equityFromTransaction(eqTx)
                .usedSector(sector != null)
                .build();

        // 3. Book value
        BookValueDTO bookValue = new BookValueDTO(latestFY.getLabel(), latestFY.getEquity());

        List<BigDecimal> candidates = new ArrayList<>();
        if (dcf != null && dcf.getEquityValue() != null) candidates.add(dcf.getEquityValue());
        if (eqEvEbitda.getMid() != null) candidates.add(eqEvEbitda.getMid());
        if (peVal.getMid() != null) candidates.add(peVal.getMid());
        if (eqRev.getMid() != null) candidates.add(eqRev.getMid());
        if (eqTx.getMid() != null) candidates.add(eqTx.getMid());
        if (bookValue.getEquityValue() != null) candidates.add(bookValue.getEquityValue());

        BigDecimal rangeLow = null;
        BigDecimal rangeHigh = null;
        BigDecimal rangeMid = null;
        if (!candidates.isEmpty()) {
            rangeLow = Collections.min(candidates);
            rangeHigh = Collections.max(candidates);
            BigDecimal sum = candidates.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            rangeMid = sum.divide(BigDecimal.valueOf(candidates.size()), 2, RoundingMode.HALF_UP);
        }

        return ValuationReportDTO.builder()
                .hasData(true)
                .dcf(dcf)
                .comparables(comparables)
                .bookValue(bookValue)
                .range(new RangeValuationDTO(rangeLow, rangeMid, rangeHigh))
                .build();
    }

    private CmaReportDTO buildCmaReport(FinancialModelDTO model) {
        if (!model.isHasData() || model.getHistoricalFYs().isEmpty()) {
            return CmaReportDTO.builder().hasData(false).build();
        }

        List<CmaOperatingRowDTO> operatingStatement = new ArrayList<>();
        List<CmaBalanceSheetRowDTO> balanceSheetAnalysis = new ArrayList<>();
        List<CmaRatioRowDTO> ratioAnalysis = new ArrayList<>();

        for (FinancialYearDTO f : model.getHistoricalFYs()) {
            BigDecimal totalExpenses = f.getCogs().add(f.getIndirectExpenseTotal()).add(f.getDepreciation()).add(f.getTax()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal cashAccruals = f.getNetProfit().add(f.getDepreciation()).setScale(2, RoundingMode.HALF_UP);
            operatingStatement.add(new CmaOperatingRowDTO(
                    f.getLabel(), f.getRevenue(), totalExpenses, f.getEbitda(), f.getDepreciation(), f.getEbit(), f.getTax(), f.getNetProfit(), cashAccruals
            ));

            BigDecimal nwc = f.getCurrentAssets().subtract(f.getCurrentLiabilities()).setScale(2, RoundingMode.HALF_UP);
            balanceSheetAnalysis.add(new CmaBalanceSheetRowDTO(
                    f.getLabel(), f.getCurrentAssets(), f.getCurrentLiabilities(), nwc, f.getDebt(), f.getEquity(), f.getTotalAssets(), f.getRatios().getDebtEquity()
            ));

            ratioAnalysis.add(new CmaRatioRowDTO(
                    f.getLabel(), f.getRatios().getCurrentRatio(), f.getRatios().getDebtEquity(), f.getRatios().getRoce(), f.getRatios().getNetMarginPct(), f.getRatios().getAssetTurnover(), null
            ));
        }

        List<CmaMpbfRowDTO> mpbf = new ArrayList<>();
        for (FinancialYearDTO pf : model.getProjectedFYs()) {
            BigDecimal wcGap = pf.getRevenue().multiply(BigDecimal.valueOf(0.20)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal marginMin = pf.getRevenue().multiply(BigDecimal.valueOf(0.05)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal mpbfVal = wcGap.subtract(marginMin).setScale(2, RoundingMode.HALF_UP);
            mpbf.add(new CmaMpbfRowDTO(pf.getLabel(), pf.getRevenue(), wcGap, marginMin, mpbfVal));
        }

        return CmaReportDTO.builder()
                .hasData(true)
                .operatingStatement(operatingStatement)
                .balanceSheetAnalysis(balanceSheetAnalysis)
                .ratioAnalysis(ratioAnalysis)
                .mpbf(mpbf)
                .dscrCaveat("DSCR is shown as \"Not Computed\" because Fin Vane ERP does not track loan repayment schedules or interest-vs-principal breakup separately. Provide this figure manually from your loan sanction letter / repayment schedule if required by your bank.")
                .mpbfCaveat("MPBF is estimated using the simplified Nayak Committee Turnover Method (20% of projected annual turnover, less 5% margin). Banks may apply the more detailed Second Method of Lending or their own internal norms -- treat this as an indicative starting point only.")
                .build();
    }

    private BigDecimal stockValueAsOf(String tenantId, String asOfDate) {
        try {
            List<Map<String, Object>> lots = jdbcTemplate.queryForList(
                    "SELECT id, qty_in, rate FROM stock_lots WHERE lot_date <= ?",
                    asOfDate
            );
            if (lots.isEmpty()) return BigDecimal.ZERO;

            List<Map<String, Object>> consumedRows = jdbcTemplate.queryForList(
                    "SELECT lot_id, SUM(consumed_qty) as consumed FROM stock_lot_consumptions " +
                    "WHERE sale_date <= ? AND lot_id IS NOT NULL GROUP BY lot_id",
                    asOfDate
            );
            Map<Long, Double> consumedMap = new HashMap<>();
            for (Map<String, Object> cr : consumedRows) {
                Number lid = (Number) cr.get("lot_id");
                Number c = (Number) cr.get("consumed");
                if (lid != null && c != null) consumedMap.put(lid.longValue(), c.doubleValue());
            }

            double val = 0.0;
            for (Map<String, Object> lot : lots) {
                Number lid = (Number) lot.get("id");
                Number qin = (Number) lot.get("qty_in");
                Number r = (Number) lot.get("rate");
                double consumed = (lid != null) ? consumedMap.getOrDefault(lid.longValue(), 0.0) : 0.0;
                double remaining = Math.max(0.0, (qin != null ? qin.doubleValue() : 0.0) - consumed);
                val += remaining * (r != null ? r.doubleValue() : 0.0);
            }
            return BigDecimal.valueOf(round2(val)).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal queryBalanceByGroupName(String tenantId, String groupName, String asOfDate) {
        try {
            return queryBigDecimal(
                    "SELECT COALESCE(SUM(CASE WHEN vi.entry_type='DEBIT' THEN vi.amount ELSE -vi.amount END), 0) " +
                    "FROM voucher_items vi " +
                    "JOIN vouchers v ON v.id = vi.voucher_id " +
                    "JOIN account_ledgers l ON l.id = vi.ledger_id " +
                    "JOIN account_groups g ON g.id = l.group_id " +
                    "WHERE v.tenant_id = ? AND v.voucher_date <= ? AND g.name = ?",
                    tenantId, asOfDate, groupName
            );
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal queryBalanceByPrimaryGroups(String tenantId, List<String> primaryGroupNames, String asOfDate) {
        try {
            String inClause = String.join("','", primaryGroupNames);
            return queryBigDecimal(
                    "SELECT COALESCE(SUM(CASE WHEN vi.entry_type='DEBIT' THEN vi.amount ELSE -vi.amount END), 0) " +
                    "FROM voucher_items vi " +
                    "JOIN vouchers v ON v.id = vi.voucher_id " +
                    "JOIN account_ledgers l ON l.id = vi.ledger_id " +
                    "JOIN account_groups g ON g.id = l.group_id " +
                    "LEFT JOIN account_groups pg ON pg.id = g.parent_id " +
                    "WHERE v.tenant_id = ? AND v.voucher_date <= ? " +
                    "  AND (g.name IN ('" + inClause + "') OR pg.name IN ('" + inClause + "'))",
                    tenantId, asOfDate
            );
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private String dayBefore(String dateStr) {
        return LocalDate.parse(dateStr).minusDays(1).toString();
    }

    private double round2(double n) {
        return Math.round((n + 1e-9) * 100.0) / 100.0;
    }

    private Double pct(BigDecimal a, BigDecimal b) {
        if (b == null || b.compareTo(BigDecimal.ZERO) == 0) return null;
        return round2(a.multiply(BigDecimal.valueOf(100)).divide(b, 4, RoundingMode.HALF_UP).doubleValue());
    }

    private BigDecimal queryBigDecimal(String sql, Object... args) {
        try {
            BigDecimal res = jdbcTemplate.queryForObject(sql, BigDecimal.class, args);
            return res != null ? res : BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private long queryLong(String sql, Object... args) {
        try {
            Long res = jdbcTemplate.queryForObject(sql, Long.class, args);
            return res != null ? res : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }
}
