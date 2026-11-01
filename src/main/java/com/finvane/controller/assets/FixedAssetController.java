package com.finvane.controller.assets;

import com.finvane.dao.accounts.IAccountLedgerJpaAccess;
import com.finvane.dao.accounts.IVoucherItemJpaAccess;
import com.finvane.dao.accounts.IVoucherJpaAccess;
import com.finvane.dao.assets.IAssetDepreciationRunJpaAccess;
import com.finvane.dao.assets.IFixedAssetJpaAccess;
import com.finvane.entity.accounts.AccountLedgerEntity;
import com.finvane.entity.accounts.VoucherEntity;
import com.finvane.entity.accounts.VoucherItemEntity;
import com.finvane.entity.assets.AssetDepreciationRunEntity;
import com.finvane.entity.assets.FixedAssetEntity;
import com.finvane.model.common.ApiResponse;
import com.finvane.model.common.TenantContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v2/assets")
@RequiredArgsConstructor
public class FixedAssetController {

    private final IFixedAssetJpaAccess assetDao;
    private final IAssetDepreciationRunJpaAccess depRunDao;
    private final IVoucherJpaAccess voucherDao;
    private final IVoucherItemJpaAccess voucherItemDao;
    private final IAccountLedgerJpaAccess ledgerDao;
    private final JdbcTemplate jdbcTemplate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FixedAssetDTO {
        private Long id;
        private String code;
        private String assetCode;
        private String name;
        private String category;
        private String department;
        private String branchName;
        private String locationName;
        private String depreciationMethod;
        private String purchaseDate;
        private String datePutToUse;
        private BigDecimal cost;
        private BigDecimal accumDep;
        private BigDecimal accumulatedDepreciation;
        private BigDecimal netBookValue;
        private BigDecimal bookValue;
        private BigDecimal projectedMonthly;
        private String status;
        private BigDecimal usefulLifeYears;
        private BigDecimal salvageValue;
        private String paymentMode;
        private String coActBlock;
        private String itActBlock;
        private BigDecimal wdvRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySummaryDTO {
        private String category;
        private long count;
        private BigDecimal netBookValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FixedAssetDashboardDTO {
        private long assetCount;
        private long activeCount;
        private BigDecimal grossBlock;
        private BigDecimal accumDep;
        private BigDecimal netBookValue;
        private BigDecimal projectedThisPeriod;
        private List<CategorySummaryDTO> byCategory;
        private String depreciationMethod;
        private AssetDepreciationRunEntity currentRun;
        private List<AssetDepreciationRunEntity> recentRuns;
    }

    private BigDecimal round2(BigDecimal val) {
        if (val == null) return BigDecimal.ZERO;
        return val.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal monthlyDepreciation(FixedAssetEntity a) {
        BigDecimal cost = a.getCost() != null ? a.getCost() : BigDecimal.ZERO;
        BigDecimal accum = a.getAccumulatedDepreciation() != null ? a.getAccumulatedDepreciation() : BigDecimal.ZERO;
        BigDecimal bv = cost.subtract(accum);
        BigDecimal floor = a.getSalvageValue() != null ? a.getSalvageValue() : BigDecimal.ZERO;
        if (bv.compareTo(floor) <= 0) return BigDecimal.ZERO;

        BigDecimal amount;
        if ("wdv".equalsIgnoreCase(a.getDepreciationMethod())) {
            BigDecimal rate = (a.getWdvRate() != null ? a.getWdvRate() : BigDecimal.ZERO)
                    .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
            amount = bv.multiply(rate).divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        } else {
            BigDecimal usefulYears = (a.getUsefulLifeYears() != null && a.getUsefulLifeYears().compareTo(BigDecimal.ZERO) > 0)
                    ? a.getUsefulLifeYears() : BigDecimal.valueOf(5);
            amount = cost.subtract(floor)
                    .divide(usefulYears.multiply(BigDecimal.valueOf(12)), 2, RoundingMode.HALF_UP);
        }
        BigDecimal remaining = bv.subtract(floor);
        return amount.min(remaining).max(BigDecimal.ZERO);
    }

    private FixedAssetDTO toDTO(FixedAssetEntity a) {
        BigDecimal cost = a.getCost() != null ? a.getCost() : BigDecimal.ZERO;
        BigDecimal accum = a.getAccumulatedDepreciation() != null ? a.getAccumulatedDepreciation() : BigDecimal.ZERO;
        BigDecimal bv = round2(cost.subtract(accum));
        BigDecimal proj = round2(monthlyDepreciation(a));

        return FixedAssetDTO.builder()
                .id(a.getId())
                .code(a.getAssetCode())
                .assetCode(a.getAssetCode())
                .name(a.getName())
                .category(a.getCategory())
                .department(a.getDepartment())
                .branchName(a.getBranchName())
                .locationName(a.getBranchName() != null ? a.getBranchName() : "Main Location")
                .depreciationMethod(a.getDepreciationMethod() != null ? a.getDepreciationMethod() : "straight_line")
                .purchaseDate(a.getPurchaseDate())
                .datePutToUse(a.getDatePutToUse() != null ? a.getDatePutToUse() : a.getPurchaseDate())
                .cost(round2(cost))
                .accumDep(round2(accum))
                .accumulatedDepreciation(round2(accum))
                .netBookValue(bv)
                .bookValue(bv)
                .projectedMonthly(proj)
                .status(a.getStatus() != null ? a.getStatus() : "active")
                .usefulLifeYears(a.getUsefulLifeYears())
                .salvageValue(a.getSalvageValue())
                .paymentMode(a.getPaymentMode())
                .coActBlock(a.getCoActBlock())
                .itActBlock(a.getItActBlock())
                .wdvRate(a.getWdvRate())
                .build();
    }

    @GetMapping
    public ApiResponse<List<FixedAssetDTO>> getAssets() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";
        List<FixedAssetEntity> entities = assetDao.findByTenantIdOrderByNameAsc(tenantId);
        List<FixedAssetDTO> dtos = new ArrayList<>();
        for (FixedAssetEntity e : entities) {
            dtos.add(toDTO(e));
        }
        return ApiResponse.ok(dtos);
    }

    @GetMapping("/dashboard")
    public ApiResponse<FixedAssetDashboardDTO> getDashboard() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";

        List<FixedAssetEntity> assets = assetDao.findByTenantIdOrderByNameAsc(tenantId);
        List<AssetDepreciationRunEntity> allRuns = depRunDao.findByTenantIdOrderByPeriodYearDescPeriodMonthDesc(tenantId);

        BigDecimal grossBlock = BigDecimal.ZERO;
        BigDecimal accumDep = BigDecimal.ZERO;
        BigDecimal projectedThisPeriod = BigDecimal.ZERO;
        long activeCount = 0;

        Map<String, CategorySummaryDTO> categoryMap = new LinkedHashMap<>();

        for (FixedAssetEntity a : assets) {
            BigDecimal c = a.getCost() != null ? a.getCost() : BigDecimal.ZERO;
            BigDecimal ad = a.getAccumulatedDepreciation() != null ? a.getAccumulatedDepreciation() : BigDecimal.ZERO;
            BigDecimal bv = c.subtract(ad);

            grossBlock = grossBlock.add(c);
            accumDep = accumDep.add(ad);

            if ("active".equalsIgnoreCase(a.getStatus())) {
                activeCount++;
                projectedThisPeriod = projectedThisPeriod.add(monthlyDepreciation(a));
            }

            String cat = (a.getCategory() != null && !a.getCategory().isBlank()) ? a.getCategory() : "Uncategorized";
            CategorySummaryDTO summary = categoryMap.computeIfAbsent(cat, k -> CategorySummaryDTO.builder()
                    .category(k)
                    .count(0)
                    .netBookValue(BigDecimal.ZERO)
                    .build());
            summary.setCount(summary.getCount() + 1);
            summary.setNetBookValue(round2(summary.getNetBookValue().add(bv)));
        }

        BigDecimal netBookValue = round2(grossBlock.subtract(accumDep));

        List<CategorySummaryDTO> byCategory = new ArrayList<>(categoryMap.values());
        byCategory.sort((c1, c2) -> c2.getNetBookValue().compareTo(c1.getNetBookValue()));

        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        AssetDepreciationRunEntity currentRun = allRuns.stream()
                .filter(r -> r.getPeriodMonth() == currentMonth && r.getPeriodYear() == currentYear)
                .findFirst()
                .orElse(null);

        List<AssetDepreciationRunEntity> recentRuns = allRuns.stream()
                .limit(6)
                .toList();

        String depMethod = "straight_line";
        if (!assets.isEmpty() && "wdv".equalsIgnoreCase(assets.get(0).getDepreciationMethod())) {
            depMethod = "wdv";
        }

        FixedAssetDashboardDTO dto = FixedAssetDashboardDTO.builder()
                .assetCount(assets.size())
                .activeCount(activeCount)
                .grossBlock(round2(grossBlock))
                .accumDep(round2(accumDep))
                .netBookValue(netBookValue)
                .projectedThisPeriod(round2(projectedThisPeriod))
                .byCategory(byCategory)
                .depreciationMethod(depMethod)
                .currentRun(currentRun)
                .recentRuns(recentRuns)
                .build();

        return ApiResponse.ok(dto);
    }

    @PostMapping
    @Transactional
    public ApiResponse<FixedAssetDTO> createAsset(@RequestBody FixedAssetEntity entity) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";
        long count = assetDao.countByTenantId(tenantId);
        if (entity.getAssetCode() == null || entity.getAssetCode().isBlank()) {
            entity.setAssetCode(String.format("FA-%03d", count + 1));
        }
        if (entity.getSalvageValue() == null && entity.getCost() != null) {
            entity.setSalvageValue(entity.getCost().multiply(BigDecimal.valueOf(0.05)).setScale(2, RoundingMode.HALF_UP));
        }
        if (entity.getDatePutToUse() == null || entity.getDatePutToUse().isBlank()) {
            entity.setDatePutToUse(entity.getPurchaseDate() != null ? entity.getPurchaseDate() : LocalDate.now().toString());
        }
        if (entity.getPurchaseDate() == null || entity.getPurchaseDate().isBlank()) {
            entity.setPurchaseDate(LocalDate.now().toString());
        }
        if (entity.getDepreciationMethod() == null || entity.getDepreciationMethod().isBlank()) {
            entity.setDepreciationMethod("straight_line");
        }
        entity.setTenantId(tenantId);
        entity.setStatus("active");
        FixedAssetEntity saved = assetDao.save(entity);

        // Auto post acquisition voucher matching Express postAssetAcquisition
        try {
            AccountLedgerEntity grossAcc = getLedgerByCode(tenantId, "1350"); // Fixed Assets - Gross Block
            AccountLedgerEntity sourceAcc = "cash".equalsIgnoreCase(entity.getPaymentMode())
                    ? getLedgerByCode(tenantId, "1000") // Cash in Hand
                    : "credit".equalsIgnoreCase(entity.getPaymentMode())
                    ? getLedgerByCode(tenantId, "2100") // Accounts Payable
                    : getLedgerByCode(tenantId, "1100"); // Bank Account - HDFC

            if (grossAcc != null && sourceAcc != null && entity.getCost() != null && entity.getCost().compareTo(BigDecimal.ZERO) > 0) {
                String vNo = String.format("FA-ACQ-%05d", saved.getId());
                VoucherEntity acqV = VoucherEntity.builder()
                        .tenantId(tenantId)
                        .voucherNumber(vNo)
                        .voucherDate(LocalDate.parse(entity.getPurchaseDate()))
                        .voucherType("ASSET_ACQUISITION")
                        .narration(String.format("Acquisition of fixed asset %s - %s", entity.getAssetCode(), entity.getName()))
                        .totalAmount(entity.getCost())
                        .build();
                VoucherEntity savedV = voucherDao.save(acqV);

                List<VoucherItemEntity> items = List.of(
                        VoucherItemEntity.builder().voucher(savedV).ledgerId(grossAcc.getId()).entryType("DEBIT").amount(entity.getCost()).lineNarration("Asset Acquisition").build(),
                        VoucherItemEntity.builder().voucher(savedV).ledgerId(sourceAcc.getId()).entryType("CREDIT").amount(entity.getCost()).lineNarration("Funding Source").build()
                );
                voucherItemDao.saveAll(items);
                saved.setAcquisitionVoucherId(savedV.getId());
                assetDao.save(saved);
            }
        } catch (Exception ignored) {}

        return ApiResponse.ok("Fixed asset registered successfully", toDTO(saved));
    }

    @PostMapping("/depreciation-method")
    @Transactional
    public ApiResponse<String> setDepreciationMethod(@RequestParam String method) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";
        String resolved = "wdv".equalsIgnoreCase(method) ? "wdv" : "straight_line";
        List<FixedAssetEntity> assets = assetDao.findByTenantIdOrderByNameAsc(tenantId);
        for (FixedAssetEntity a : assets) {
            a.setDepreciationMethod(resolved);
        }
        assetDao.saveAll(assets);
        return ApiResponse.ok("Depreciation method updated to " + resolved, resolved);
    }

    @PostMapping("/push-to-reports")
    @Transactional
    public ApiResponse<Map<String, Object>> pushToReports() {
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();
        AssetDepreciationRunEntity run = runDepreciationInternal(month, year);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("month", month);
        resp.put("year", year);
        resp.put("totalAmount", run.getTotalAmount());
        return ApiResponse.ok("Pushed current period depreciation to reports", resp);
    }

    @GetMapping("/depreciation-runs")
    public ApiResponse<List<AssetDepreciationRunEntity>> getDepreciationRuns() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";
        return ApiResponse.ok(depRunDao.findByTenantIdOrderByPeriodYearDescPeriodMonthDesc(tenantId));
    }

    @PostMapping("/run-depreciation")
    @Transactional
    public ApiResponse<AssetDepreciationRunEntity> runDepreciation(@RequestParam int month, @RequestParam int year) {
        AssetDepreciationRunEntity run = runDepreciationInternal(month, year);
        return ApiResponse.ok("Depreciation run posted successfully", run);
    }

    private AssetDepreciationRunEntity runDepreciationInternal(int month, int year) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";
        List<FixedAssetEntity> assets = assetDao.findByTenantIdOrderByNameAsc(tenantId);

        BigDecimal totalDep = BigDecimal.ZERO;
        for (FixedAssetEntity fa : assets) {
            if (!"active".equalsIgnoreCase(fa.getStatus())) continue;
            BigDecimal monthlyDep = monthlyDepreciation(fa);
            totalDep = totalDep.add(monthlyDep);
            fa.setAccumulatedDepreciation(fa.getAccumulatedDepreciation() != null
                    ? fa.getAccumulatedDepreciation().add(monthlyDep) : monthlyDep);
            assetDao.save(fa);
        }

        AccountLedgerEntity depExpense = getLedgerByCode(tenantId, "5900"); // Depreciation expense (code 5900)
        AccountLedgerEntity accumDep = getLedgerByCode(tenantId, "1360"); // Accumulated depreciation (code 1360)

        String voucherNo = String.format("DEP-JV-%04d-%02d", year, month);

        // Check if an existing run exists for this month/year
        Optional<AssetDepreciationRunEntity> existingRunOpt = depRunDao.findByTenantIdOrderByPeriodYearDescPeriodMonthDesc(tenantId).stream()
                .filter(r -> r.getPeriodMonth() == month && r.getPeriodYear() == year)
                .findFirst();

        if (existingRunOpt.isPresent()) {
            AssetDepreciationRunEntity existingRun = existingRunOpt.get();
            existingRun.setTotalAmount(totalDep);
            if (existingRun.getVoucherId() != null) {
                Optional<VoucherEntity> vOpt = voucherDao.findById(existingRun.getVoucherId());
                if (vOpt.isPresent()) {
                    VoucherEntity v = vOpt.get();
                    v.setTotalAmount(totalDep);
                    voucherDao.save(v);
                    if (v.getItems() != null) {
                        for (VoucherItemEntity item : v.getItems()) {
                            item.setAmount(totalDep);
                            voucherItemDao.save(item);
                        }
                    }
                }
            }
            return depRunDao.save(existingRun);
        }

        VoucherEntity v = VoucherEntity.builder()
                .tenantId(tenantId)
                .voucherNumber(voucherNo)
                .voucherDate(LocalDate.now())
                .voucherType("DEPRECIATION")
                .narration(String.format("Depreciation for %02d/%04d across %d asset(s)", month, year, assets.size()))
                .totalAmount(totalDep)
                .build();
        VoucherEntity savedV = voucherDao.save(v);

        List<VoucherItemEntity> items = new ArrayList<>();
        if (depExpense != null) {
            items.add(VoucherItemEntity.builder().voucher(savedV).ledgerId(depExpense.getId()).entryType("DEBIT").amount(totalDep).lineNarration("Monthly Depreciation").build());
        }
        if (accumDep != null) {
            items.add(VoucherItemEntity.builder().voucher(savedV).ledgerId(accumDep.getId()).entryType("CREDIT").amount(totalDep).lineNarration("Accumulated Depreciation").build());
        }
        voucherItemDao.saveAll(items);

        AssetDepreciationRunEntity run = AssetDepreciationRunEntity.builder()
                .tenantId(tenantId)
                .periodMonth(month)
                .periodYear(year)
                .voucherId(savedV.getId())
                .totalAmount(totalDep)
                .build();
        return depRunDao.save(run);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<String> deleteAsset(@PathVariable Long id) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";
        Optional<FixedAssetEntity> opt = assetDao.findById(id);
        if (opt.isEmpty() || !tenantId.equals(opt.get().getTenantId())) {
            return ApiResponse.error("Fixed asset not found");
        }
        FixedAssetEntity a = opt.get();
        if (a.getAcquisitionVoucherId() != null) {
            try {
                voucherDao.deleteById(a.getAcquisitionVoucherId());
            } catch (Exception ignored) {}
        }
        assetDao.delete(a);
        return ApiResponse.ok("Asset deleted successfully", a.getAssetCode());
    }

    @PostMapping("/delete-all")
    @Transactional
    public ApiResponse<String> deleteAll() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";
        List<FixedAssetEntity> assets = assetDao.findByTenantIdOrderByNameAsc(tenantId);
        long deletedCount = 0;
        for (FixedAssetEntity a : assets) {
            if (a.getAcquisitionVoucherId() != null) {
                try {
                    voucherDao.deleteById(a.getAcquisitionVoucherId());
                } catch (Exception ignored) {}
            }
            assetDao.delete(a);
            deletedCount++;
        }
        return ApiResponse.ok("Deleted " + deletedCount + " assets", String.valueOf(deletedCount));
    }

    private AccountLedgerEntity getLedgerByCode(String tenantId, String code) {
        return ledgerDao.findByTenantIdAndIsActiveTrueOrderByNameAsc(tenantId).stream()
                .filter(l -> code.equals(l.getCode()))
                .findFirst()
                .orElse(null);
    }
}
