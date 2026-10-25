package com.finvane.controller.budgets;

import com.finvane.dao.accounts.IAccountGroupJpaAccess;
import com.finvane.dao.accounts.IAccountLedgerJpaAccess;
import com.finvane.dao.accounts.IVoucherJpaAccess;
import com.finvane.dao.budgets.IBudgetJpaAccess;
import com.finvane.entity.accounts.AccountGroupEntity;
import com.finvane.entity.accounts.AccountLedgerEntity;
import com.finvane.entity.accounts.VoucherEntity;
import com.finvane.entity.accounts.VoucherItemEntity;
import com.finvane.entity.budgets.BudgetEntity;
import com.finvane.model.budgets.BudgetEntryDTO;
import com.finvane.model.budgets.BudgetPlanDTO;
import com.finvane.model.budgets.BudgetPlanSaveDTO;
import com.finvane.model.common.ApiResponse;
import com.finvane.model.common.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final IBudgetJpaAccess budgetDao;
    private final IAccountLedgerJpaAccess ledgerDao;
    private final IAccountGroupJpaAccess groupDao;
    private final IVoucherJpaAccess voucherDao;

    @GetMapping
    public ApiResponse<List<BudgetEntryDTO>> getBudgets(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        String tenantId = TenantContext.getTenantId();
        if (from == null || from.isBlank()) from = "2026-04-01";
        if (to == null || to.isBlank()) to = "2027-03-31";

        List<BudgetEntity> budgets = budgetDao.findByTenantIdAndPeriodStartAndPeriodEnd(tenantId, from, to);
        List<AccountLedgerEntity> ledgers = ledgerDao.findByTenantIdAndIsActiveTrueOrderByNameAsc(tenantId);
        List<AccountGroupEntity> groups = groupDao.findByTenantIdOrderByNameAsc(tenantId);

        Map<Long, AccountLedgerEntity> ledgerMap = ledgers.stream()
                .collect(Collectors.toMap(AccountLedgerEntity::getId, l -> l, (a, b) -> a));
        Map<Long, AccountGroupEntity> groupMap = groups.stream()
                .collect(Collectors.toMap(AccountGroupEntity::getId, g -> g, (a, b) -> a));

        // Compute actual balances from posted vouchers within window
        Map<Long, BigDecimal> actualMap = new HashMap<>();
        List<VoucherEntity> vouchers = voucherDao.findByTenantId(tenantId);
        LocalDate startDt = LocalDate.parse(from);
        LocalDate endDt = LocalDate.parse(to);

        for (VoucherEntity v : vouchers) {
            if (v.getVoucherDate() == null) continue;
            if (v.getVoucherDate().isBefore(startDt) || v.getVoucherDate().isAfter(endDt)) continue;
            if (v.getItems() != null) {
                for (VoucherItemEntity vi : v.getItems()) {
                    if (vi.getLedgerId() == null || vi.getAmount() == null) continue;
                    BigDecimal amt = vi.getAmount() != null ? vi.getAmount() : BigDecimal.ZERO;
                    BigDecimal net = "DEBIT".equalsIgnoreCase(vi.getEntryType()) ? amt : amt.negate();
                    actualMap.merge(vi.getLedgerId(), net, BigDecimal::add);
                }
            }
        }

        List<BudgetEntryDTO> result = new ArrayList<>();
        for (BudgetEntity b : budgets) {
            AccountLedgerEntity l = ledgerMap.get(b.getAccountId());
            String code = l != null ? l.getCode() : "";
            String name = l != null ? l.getName() : "";
            String type = "expense";
            if (l != null) {
                AccountGroupEntity g = groupMap.get(l.getGroupId());
                if (g != null && "INCOME".equalsIgnoreCase(g.getNature())) {
                    type = "income";
                }
            }

            BigDecimal netBal = actualMap.getOrDefault(b.getAccountId(), BigDecimal.ZERO);
            BigDecimal actual = "income".equals(type) ? netBal.negate() : netBal;
            BigDecimal variance = actual.subtract(b.getAmount());
            BigDecimal variancePct = b.getAmount().compareTo(BigDecimal.ZERO) != 0
                    ? variance.multiply(BigDecimal.valueOf(100)).divide(b.getAmount(), 2, RoundingMode.HALF_UP)
                    : null;

            result.add(BudgetEntryDTO.builder()
                    .id(b.getId())
                    .accountId(b.getAccountId())
                    .accountCode(code)
                    .accountName(name)
                    .accountType(type)
                    .periodStart(b.getPeriodStart())
                    .periodEnd(b.getPeriodEnd())
                    .amount(b.getAmount())
                    .actual(actual)
                    .variance(variance)
                    .variancePct(variancePct)
                    .notes(b.getNotes())
                    .build());
        }

        result.sort(Comparator.comparing(BudgetEntryDTO::getAccountCode));
        return ApiResponse.ok(result);
    }

    @PostMapping
    public ApiResponse<BudgetEntryDTO> createBudget(@RequestBody BudgetEntryDTO dto) {
        String tenantId = TenantContext.getTenantId();
        Optional<BudgetEntity> existing = budgetDao.findByTenantIdAndAccountIdAndPeriodStartAndPeriodEnd(
                tenantId, dto.getAccountId(), dto.getPeriodStart(), dto.getPeriodEnd());

        BudgetEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            entity.setAmount(dto.getAmount());
            entity.setNotes(dto.getNotes());
        } else {
            entity = BudgetEntity.builder()
                    .tenantId(tenantId)
                    .accountId(dto.getAccountId())
                    .periodStart(dto.getPeriodStart())
                    .periodEnd(dto.getPeriodEnd())
                    .amount(dto.getAmount())
                    .notes(dto.getNotes())
                    .build();
        }
        BudgetEntity saved = budgetDao.save(entity);
        dto.setId(saved.getId());
        return ApiResponse.ok("Budget entry saved successfully", dto);
    }

    @GetMapping("/{id}")
    public ApiResponse<BudgetEntryDTO> getBudgetById(@PathVariable Long id) {
        BudgetEntity b = budgetDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Budget entry not found with ID: " + id));

        AccountLedgerEntity l = ledgerDao.findById(b.getAccountId()).orElse(null);
        BudgetEntryDTO dto = BudgetEntryDTO.builder()
                .id(b.getId())
                .accountId(b.getAccountId())
                .accountCode(l != null ? l.getCode() : "")
                .accountName(l != null ? l.getName() : "")
                .periodStart(b.getPeriodStart())
                .periodEnd(b.getPeriodEnd())
                .amount(b.getAmount())
                .notes(b.getNotes())
                .build();
        return ApiResponse.ok(dto);
    }

    @PutMapping("/{id}")
    public ApiResponse<BudgetEntryDTO> updateBudget(@PathVariable Long id, @RequestBody BudgetEntryDTO dto) {
        String tenantId = TenantContext.getTenantId();
        BudgetEntity entity = budgetDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Budget entry not found with ID: " + id));

        Optional<BudgetEntity> clash = budgetDao.findByTenantIdAndAccountIdAndPeriodStartAndPeriodEnd(
                tenantId, dto.getAccountId(), dto.getPeriodStart(), dto.getPeriodEnd());
        if (clash.isPresent() && !clash.get().getId().equals(id)) {
            throw new IllegalArgumentException("An entry for this account and period already exists.");
        }

        entity.setAccountId(dto.getAccountId());
        entity.setPeriodStart(dto.getPeriodStart());
        entity.setPeriodEnd(dto.getPeriodEnd());
        entity.setAmount(dto.getAmount());
        entity.setNotes(dto.getNotes());
        budgetDao.save(entity);

        dto.setId(id);
        return ApiResponse.ok("Budget entry updated successfully", dto);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBudget(@PathVariable Long id) {
        budgetDao.deleteById(id);
        return ApiResponse.ok("Budget entry deleted successfully", null);
    }

    @GetMapping("/plan")
    public ApiResponse<BudgetPlanDTO> getPlan(@RequestParam(required = false, defaultValue = "2026") int fy) {
        String tenantId = TenantContext.getTenantId();

        List<BudgetPlanDTO.BudgetMonthHeader> months = new ArrayList<>();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("MMM ''yy", Locale.ENGLISH);
        for (int m = 0; m < 12; m++) {
            int calMonth = (m + 4 <= 12) ? (m + 4) : (m - 8);
            int calYear = (m + 4 <= 12) ? fy : (fy + 1);
            LocalDate start = LocalDate.of(calYear, calMonth, 1);
            LocalDate end = start.plusMonths(1).minusDays(1);
            months.add(BudgetPlanDTO.BudgetMonthHeader.builder()
                    .label(start.format(df).toUpperCase())
                    .periodStart(start.toString())
                    .periodEnd(end.toString())
                    .build());
        }

        List<AccountLedgerEntity> ledgers = ledgerDao.findByTenantIdAndIsActiveTrueOrderByNameAsc(tenantId);
        List<AccountGroupEntity> groups = groupDao.findByTenantIdOrderByNameAsc(tenantId);
        Map<Long, AccountGroupEntity> groupMap = groups.stream()
                .collect(Collectors.toMap(AccountGroupEntity::getId, g -> g, (a, b) -> a));

        List<AccountLedgerEntity> planLedgers = ledgers.stream().filter(l -> {
            AccountGroupEntity g = groupMap.get(l.getGroupId());
            if (g == null) return false;
            String n = g.getNature();
            return "EXPENSE".equalsIgnoreCase(n) || "INCOME".equalsIgnoreCase(n);
        }).sorted((a, b) -> {
            AccountGroupEntity ga = groupMap.get(a.getGroupId());
            AccountGroupEntity gb = groupMap.get(b.getGroupId());
            String na = ga != null ? ga.getNature() : "";
            String nb = gb != null ? gb.getNature() : "";
            int cmp = na.compareToIgnoreCase(nb); // Expense first if sorted
            if (cmp != 0) return cmp;
            return a.getCode().compareTo(b.getCode());
        }).toList();

        List<BudgetEntity> allBudgets = budgetDao.findByTenantId(tenantId);
        Map<String, BigDecimal> budgetMap = new HashMap<>();
        for (BudgetEntity b : allBudgets) {
            budgetMap.put(b.getAccountId() + ":" + b.getPeriodStart(), b.getAmount());
        }

        List<BudgetPlanDTO.BudgetPlanRow> grid = new ArrayList<>();
        BigDecimal incomeTotal = BigDecimal.ZERO;
        BigDecimal expenseTotal = BigDecimal.ZERO;
        List<BigDecimal> monthTotals = new ArrayList<>(Collections.nCopies(12, BigDecimal.ZERO));

        for (AccountLedgerEntity l : planLedgers) {
            AccountGroupEntity g = groupMap.get(l.getGroupId());
            String type = (g != null && "INCOME".equalsIgnoreCase(g.getNature())) ? "income" : "expense";

            List<BudgetPlanDTO.BudgetCell> cells = new ArrayList<>();
            BigDecimal rowTotal = BigDecimal.ZERO;
            for (int i = 0; i < 12; i++) {
                String pStart = months.get(i).getPeriodStart();
                BigDecimal amt = budgetMap.getOrDefault(l.getId() + ":" + pStart, BigDecimal.ZERO);
                cells.add(BudgetPlanDTO.BudgetCell.builder().periodStart(pStart).amount(amt).build());
                rowTotal = rowTotal.add(amt);
                monthTotals.set(i, monthTotals.get(i).add(amt));
            }

            if ("income".equals(type)) {
                incomeTotal = incomeTotal.add(rowTotal);
            } else {
                expenseTotal = expenseTotal.add(rowTotal);
            }

            grid.add(BudgetPlanDTO.BudgetPlanRow.builder()
                    .id(l.getId())
                    .code(l.getCode())
                    .name(l.getName())
                    .type(type)
                    .cells(cells)
                    .rowTotal(rowTotal)
                    .build());
        }

        BudgetPlanDTO dto = BudgetPlanDTO.builder()
                .fy(fy)
                .months(months)
                .grid(grid)
                .monthTotals(monthTotals)
                .incomeTotal(incomeTotal)
                .expenseTotal(expenseTotal)
                .netPlanned(incomeTotal.subtract(expenseTotal))
                .build();

        return ApiResponse.ok(dto);
    }

    @PostMapping("/plan")
    @Transactional
    public ApiResponse<Void> savePlan(@RequestBody BudgetPlanSaveDTO saveDto) {
        String tenantId = TenantContext.getTenantId();
        int fy = saveDto.getFy() > 0 ? saveDto.getFy() : 2026;

        Map<String, String> monthEndByStart = new HashMap<>();
        for (int m = 0; m < 12; m++) {
            int calMonth = (m + 4 <= 12) ? (m + 4) : (m - 8);
            int calYear = (m + 4 <= 12) ? fy : (fy + 1);
            LocalDate start = LocalDate.of(calYear, calMonth, 1);
            LocalDate end = start.plusMonths(1).minusDays(1);
            monthEndByStart.put(start.toString(), end.toString());
        }

        if (saveDto.getAmounts() != null) {
            for (Map.Entry<String, Map<String, BigDecimal>> entry : saveDto.getAmounts().entrySet()) {
                Long accountId = Long.parseLong(entry.getKey().replace("a", ""));
                for (Map.Entry<String, BigDecimal> cell : entry.getValue().entrySet()) {
                    String periodStart = cell.getKey();
                    String periodEnd = monthEndByStart.get(periodStart);
                    if (periodEnd == null) continue;

                    BigDecimal amt = cell.getValue();
                    if (amt == null || amt.compareTo(BigDecimal.ZERO) == 0) {
                        budgetDao.deleteByTenantIdAndAccountIdAndPeriodStartAndPeriodEnd(tenantId, accountId, periodStart, periodEnd);
                    } else {
                        Optional<BudgetEntity> exist = budgetDao.findByTenantIdAndAccountIdAndPeriodStartAndPeriodEnd(
                                tenantId, accountId, periodStart, periodEnd);
                        if (exist.isPresent()) {
                            BudgetEntity be = exist.get();
                            be.setAmount(amt);
                            budgetDao.save(be);
                        } else {
                            budgetDao.save(BudgetEntity.builder()
                                    .tenantId(tenantId)
                                    .accountId(accountId)
                                    .periodStart(periodStart)
                                    .periodEnd(periodEnd)
                                    .amount(amt)
                                    .build());
                        }
                    }
                }
            }
        }
        return ApiResponse.ok("Budget plan saved successfully", null);
    }
}
