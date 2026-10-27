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
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v2/assets")
@RequiredArgsConstructor
public class FixedAssetController {

    private final IFixedAssetJpaAccess assetDao;
    private final IAssetDepreciationRunJpaAccess depRunDao;
    private final IVoucherJpaAccess voucherDao;
    private final IVoucherItemJpaAccess voucherItemDao;
    private final IAccountLedgerJpaAccess ledgerDao;

    @GetMapping
    public ApiResponse<List<FixedAssetEntity>> getAssets() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";
        return ApiResponse.ok(assetDao.findByTenantIdOrderByNameAsc(tenantId));
    }

    @PostMapping
    public ApiResponse<FixedAssetEntity> createAsset(@RequestBody FixedAssetEntity entity) {
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
            entity.setDatePutToUse(entity.getPurchaseDate());
        }
        entity.setTenantId(tenantId);
        entity.setStatus("active");
        FixedAssetEntity saved = assetDao.save(entity);
        return ApiResponse.ok("Fixed asset registered successfully", saved);
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
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";
        List<FixedAssetEntity> assets = assetDao.findByTenantIdOrderByNameAsc(tenantId);

        BigDecimal totalDep = BigDecimal.ZERO;
        for (FixedAssetEntity fa : assets) {
            if (!"active".equalsIgnoreCase(fa.getStatus())) continue;
            BigDecimal cost = fa.getCost() != null ? fa.getCost() : BigDecimal.ZERO;
            BigDecimal salvage = fa.getSalvageValue() != null ? fa.getSalvageValue() : BigDecimal.ZERO;
            BigDecimal usefulYears = fa.getUsefulLifeYears() != null && fa.getUsefulLifeYears().compareTo(BigDecimal.ZERO) > 0
                    ? fa.getUsefulLifeYears() : BigDecimal.valueOf(5);

            // Monthly SLM = (cost - salvage) / (usefulYears * 12)
            BigDecimal monthlyDep = cost.subtract(salvage)
                    .divide(usefulYears.multiply(BigDecimal.valueOf(12)), 2, RoundingMode.HALF_UP);

            totalDep = totalDep.add(monthlyDep);
            fa.setAccumulatedDepreciation(fa.getAccumulatedDepreciation() != null
                    ? fa.getAccumulatedDepreciation().add(monthlyDep) : monthlyDep);
            assetDao.save(fa);
        }

        // Auto post depreciation voucher
        AccountLedgerEntity depExpense = getLedgerByCode(tenantId, "5300"); // Depreciation expense
        AccountLedgerEntity accumDep = getLedgerByCode(tenantId, "1600"); // Accumulated depreciation

        String voucherNo = String.format("DEP-%04d%02d", year, month);
        VoucherEntity v = VoucherEntity.builder()
                .tenantId(tenantId)
                .voucherNumber(voucherNo)
                .voucherDate(LocalDate.now())
                .voucherType("DEPRECIATION")
                .narration(String.format("Depreciation run for %02d/%04d", month, year))
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
        AssetDepreciationRunEntity savedRun = depRunDao.save(run);

        return ApiResponse.ok("Depreciation run posted successfully", savedRun);
    }

    private AccountLedgerEntity getLedgerByCode(String tenantId, String code) {
        return ledgerDao.findByTenantIdAndIsActiveTrueOrderByNameAsc(tenantId).stream()
                .filter(l -> code.equals(l.getCode()))
                .findFirst()
                .orElse(null);
    }
}
