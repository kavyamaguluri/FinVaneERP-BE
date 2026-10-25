package com.finvane.controller.reports;

import com.finvane.dao.accounts.IAccountGroupJpaAccess;
import com.finvane.dao.accounts.IAccountLedgerJpaAccess;
import com.finvane.dao.accounts.IVoucherJpaAccess;
import com.finvane.dao.common.IEntityJpaAccess;
import com.finvane.entity.accounts.AccountGroupEntity;
import com.finvane.entity.accounts.AccountLedgerEntity;
import com.finvane.entity.accounts.VoucherEntity;
import com.finvane.entity.accounts.VoucherItemEntity;
import com.finvane.entity.common.EntityEntity;
import com.finvane.model.common.ApiResponse;
import com.finvane.model.common.TenantContext;
import com.finvane.model.reports.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/reports")
@RequiredArgsConstructor
public class ReportController {

    private final IAccountLedgerJpaAccess ledgerDao;
    private final IAccountGroupJpaAccess groupDao;
    private final IVoucherJpaAccess voucherDao;
    private final IEntityJpaAccess entityDao;

    @GetMapping("/trial-balance")
    public ApiResponse<TrialBalanceDTO> getTrialBalance(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        String tenantId = TenantContext.getTenantId();
        if (from == null || from.isBlank()) from = "2026-04-01";
        if (to == null || to.isBlank()) to = "2027-03-31";

        List<AccountLedgerEntity> ledgers = ledgerDao.findByTenantIdAndIsActiveTrueOrderByNameAsc(tenantId);
        List<AccountGroupEntity> groups = groupDao.findByTenantIdOrderByNameAsc(tenantId);
        Map<Long, AccountGroupEntity> groupMap = groups.stream()
                .collect(Collectors.toMap(AccountGroupEntity::getId, g -> g, (a, b) -> a));

        Map<Long, BigDecimal> debitMap = new HashMap<>();
        Map<Long, BigDecimal> creditMap = new HashMap<>();

        List<VoucherEntity> vouchers = voucherDao.findByTenantId(tenantId);
        LocalDate startDt = LocalDate.parse(from);
        LocalDate endDt = LocalDate.parse(to);

        for (VoucherEntity v : vouchers) {
            if (v.getVoucherDate() == null) continue;
            if (v.getVoucherDate().isBefore(startDt) || v.getVoucherDate().isAfter(endDt)) continue;
            if (v.getItems() != null) {
                for (VoucherItemEntity vi : v.getItems()) {
                    if (vi.getLedgerId() == null) continue;
                    BigDecimal amt = vi.getAmount() != null ? vi.getAmount() : BigDecimal.ZERO;
                    BigDecimal d = "DEBIT".equalsIgnoreCase(vi.getEntryType()) ? amt : BigDecimal.ZERO;
                    BigDecimal c = "CREDIT".equalsIgnoreCase(vi.getEntryType()) ? amt : BigDecimal.ZERO;
                    debitMap.merge(vi.getLedgerId(), d, BigDecimal::add);
                    creditMap.merge(vi.getLedgerId(), c, BigDecimal::add);
                }
            }
        }

        BigDecimal grandDebit = BigDecimal.ZERO;
        BigDecimal grandCredit = BigDecimal.ZERO;

        Map<String, List<TrialBalanceDTO.TrialBalanceRow>> rowsByGroup = new LinkedHashMap<>();
        for (AccountLedgerEntity l : ledgers) {
            AccountGroupEntity g = groupMap.get(l.getGroupId());
            String gName = g != null ? g.getName() : "General";

            BigDecimal d = debitMap.getOrDefault(l.getId(), BigDecimal.ZERO);
            BigDecimal c = creditMap.getOrDefault(l.getId(), BigDecimal.ZERO);

            BigDecimal op = l.getOpeningBalance() != null ? l.getOpeningBalance() : BigDecimal.ZERO;
            if (op.compareTo(BigDecimal.ZERO) >= 0) {
                d = d.add(op);
            } else {
                c = c.add(op.abs());
            }

            BigDecimal net = d.subtract(c);
            grandDebit = grandDebit.add(d);
            grandCredit = grandCredit.add(c);

            rowsByGroup.computeIfAbsent(gName, k -> new ArrayList<>()).add(
                    TrialBalanceDTO.TrialBalanceRow.builder()
                            .id(l.getId())
                            .code(l.getCode())
                            .name(l.getName())
                            .debit(d)
                            .credit(c)
                            .netBalance(net)
                            .build()
            );
        }

        List<TrialBalanceDTO.TrialBalanceGroup> groupDTOs = new ArrayList<>();
        for (Map.Entry<String, List<TrialBalanceDTO.TrialBalanceRow>> e : rowsByGroup.entrySet()) {
            BigDecimal grpD = e.getValue().stream().map(TrialBalanceDTO.TrialBalanceRow::getDebit).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal grpC = e.getValue().stream().map(TrialBalanceDTO.TrialBalanceRow::getCredit).reduce(BigDecimal.ZERO, BigDecimal::add);
            groupDTOs.add(TrialBalanceDTO.TrialBalanceGroup.builder()
                    .groupName(e.getKey())
                    .totalDebit(grpD)
                    .totalCredit(grpC)
                    .accounts(e.getValue())
                    .build());
        }

        TrialBalanceDTO dto = TrialBalanceDTO.builder()
                .fromDate(from)
                .toDate(to)
                .totalDebit(grandDebit)
                .totalCredit(grandCredit)
                .isBalanced(grandDebit.compareTo(grandCredit) == 0)
                .groups(groupDTOs)
                .build();

        return ApiResponse.ok(dto);
    }

    @GetMapping("/profit-loss")
    public ApiResponse<ProfitLossDTO> getProfitLoss(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        String tenantId = TenantContext.getTenantId();
        if (from == null || from.isBlank()) from = "2026-04-01";
        if (to == null || to.isBlank()) to = "2027-03-31";

        List<AccountLedgerEntity> ledgers = ledgerDao.findByTenantIdAndIsActiveTrueOrderByNameAsc(tenantId);
        List<AccountGroupEntity> groups = groupDao.findByTenantIdOrderByNameAsc(tenantId);
        Map<Long, AccountGroupEntity> groupMap = groups.stream()
                .collect(Collectors.toMap(AccountGroupEntity::getId, g -> g, (a, b) -> a));

        List<ProfitLossDTO.ReportLine> revLines = new ArrayList<>();
        List<ProfitLossDTO.ReportLine> expLines = new ArrayList<>();

        BigDecimal totalRev = BigDecimal.ZERO;
        BigDecimal totalExp = BigDecimal.ZERO;

        for (AccountLedgerEntity l : ledgers) {
            AccountGroupEntity g = groupMap.get(l.getGroupId());
            if (g == null) continue;
            BigDecimal bal = l.getCurrentBalance() != null ? l.getCurrentBalance() : BigDecimal.ZERO;
            if (bal.compareTo(BigDecimal.ZERO) == 0) continue;

            if ("INCOME".equalsIgnoreCase(g.getNature())) {
                revLines.add(ProfitLossDTO.ReportLine.builder().code(l.getCode()).name(l.getName()).amount(bal).build());
                totalRev = totalRev.add(bal);
            } else if ("EXPENSE".equalsIgnoreCase(g.getNature())) {
                expLines.add(ProfitLossDTO.ReportLine.builder().code(l.getCode()).name(l.getName()).amount(bal).build());
                totalExp = totalExp.add(bal);
            }
        }

        BigDecimal netProfit = totalRev.subtract(totalExp);

        ProfitLossDTO dto = ProfitLossDTO.builder()
                .fromDate(from)
                .toDate(to)
                .revenueFromOperations(totalRev)
                .otherIncome(BigDecimal.ZERO)
                .totalRevenue(totalRev)
                .costOfMaterials(BigDecimal.ZERO)
                .purchaseOfStock(BigDecimal.ZERO)
                .employeeBenefitExpense(BigDecimal.ZERO)
                .depreciationExpense(BigDecimal.ZERO)
                .otherExpenses(totalExp)
                .totalExpenses(totalExp)
                .profitBeforeTax(netProfit)
                .taxExpense(BigDecimal.ZERO)
                .netProfit(netProfit)
                .revenueLines(revLines)
                .expenseLines(expLines)
                .build();

        return ApiResponse.ok(dto);
    }

    @GetMapping("/balance-sheet")
    public ApiResponse<BalanceSheetDTO> getBalanceSheet(@RequestParam(required = false) String to) {
        String tenantId = TenantContext.getTenantId();
        if (to == null || to.isBlank()) to = LocalDate.now().toString();

        List<AccountLedgerEntity> ledgers = ledgerDao.findByTenantIdAndIsActiveTrueOrderByNameAsc(tenantId);
        List<AccountGroupEntity> groups = groupDao.findByTenantIdOrderByNameAsc(tenantId);
        Map<Long, AccountGroupEntity> groupMap = groups.stream()
                .collect(Collectors.toMap(AccountGroupEntity::getId, g -> g, (a, b) -> a));

        List<BalanceSheetDTO.BalanceSheetLine> assetLines = new ArrayList<>();
        List<BalanceSheetDTO.BalanceSheetLine> liabLines = new ArrayList<>();

        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalLiab = BigDecimal.ZERO;

        for (AccountLedgerEntity l : ledgers) {
            AccountGroupEntity g = groupMap.get(l.getGroupId());
            if (g == null) continue;
            BigDecimal bal = l.getCurrentBalance() != null ? l.getCurrentBalance() : BigDecimal.ZERO;

            if ("ASSET".equalsIgnoreCase(g.getNature())) {
                assetLines.add(BalanceSheetDTO.BalanceSheetLine.builder().code(l.getCode()).name(l.getName()).amount(bal).build());
                totalAssets = totalAssets.add(bal);
            } else if ("LIABILITY".equalsIgnoreCase(g.getNature()) || "EQUITY".equalsIgnoreCase(g.getNature())) {
                liabLines.add(BalanceSheetDTO.BalanceSheetLine.builder().code(l.getCode()).name(l.getName()).amount(bal).build());
                totalLiab = totalLiab.add(bal);
            }
        }

        BalanceSheetDTO dto = BalanceSheetDTO.builder()
                .asOfDate(to)
                .totalAssets(totalAssets)
                .totalLiabilitiesAndEquity(totalLiab)
                .isBalanced(totalAssets.compareTo(totalLiab) == 0)
                .assets(List.of(BalanceSheetDTO.BalanceSheetSection.builder().title("Assets").total(totalAssets).lines(assetLines).build()))
                .equityAndLiabilities(List.of(BalanceSheetDTO.BalanceSheetSection.builder().title("Equities & Liabilities").total(totalLiab).lines(liabLines).build()))
                .build();

        return ApiResponse.ok(dto);
    }

    @GetMapping("/cash-flow")
    public ApiResponse<CashFlowDTO> getCashFlow(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        String tenantId = TenantContext.getTenantId();
        if (from == null || from.isBlank()) from = "2026-04-01";
        if (to == null || to.isBlank()) to = "2027-03-31";

        List<AccountLedgerEntity> ledgers = ledgerDao.findByTenantIdAndIsActiveTrueOrderByNameAsc(tenantId);
        List<AccountGroupEntity> groups = groupDao.findByTenantIdOrderByNameAsc(tenantId);
        Map<Long, AccountGroupEntity> groupMap = groups.stream()
                .collect(Collectors.toMap(AccountGroupEntity::getId, g -> g, (a, b) -> a));

        BigDecimal cashBalance = BigDecimal.ZERO;
        for (AccountLedgerEntity l : ledgers) {
            AccountGroupEntity g = groupMap.get(l.getGroupId());
            if (g != null && "ASSET".equalsIgnoreCase(g.getNature())) {
                String n = l.getName().toLowerCase();
                if (n.contains("cash") || n.contains("bank") || l.getCode().startsWith("10") || l.getCode().startsWith("11")) {
                    cashBalance = cashBalance.add(l.getCurrentBalance() != null ? l.getCurrentBalance() : BigDecimal.ZERO);
                }
            }
        }

        CashFlowDTO dto = CashFlowDTO.builder()
                .fromDate(from)
                .toDate(to)
                .operatingCashFlow(cashBalance)
                .investingCashFlow(BigDecimal.ZERO)
                .financingCashFlow(BigDecimal.ZERO)
                .netCashFlow(cashBalance)
                .openingCash(BigDecimal.ZERO)
                .closingCash(cashBalance)
                .operatingItems(List.of(CashFlowDTO.CashFlowItem.builder().description("Net cash from operations").amount(cashBalance).build()))
                .investingItems(Collections.emptyList())
                .financingItems(Collections.emptyList())
                .build();

        return ApiResponse.ok(dto);
    }

    @GetMapping("/financials")
    public ApiResponse<FinancialsSchedule3DTO> getFinancialsSchedule3(@RequestParam(required = false) String to) {
        String tenantId = TenantContext.getTenantId();
        Optional<EntityEntity> ent = entityDao.findById(Long.parseLong(tenantId));
        String name = ent.isPresent() ? ent.get().getName() : "Enterprise";
        String cin = "U72200MH2026PTC123456";
        String pan = ent.isPresent() && ent.get().getPan() != null ? ent.get().getPan() : "AABCF1234F";

        BalanceSheetDTO bs = getBalanceSheet(to).getData();
        ProfitLossDTO pl = getProfitLoss("2026-04-01", to).getData();
        CashFlowDTO cf = getCashFlow("2026-04-01", to).getData();

        List<FinancialsSchedule3DTO.Schedule3Note> notes = List.of(
                FinancialsSchedule3DTO.Schedule3Note.builder()
                        .noteNumber("1")
                        .title("Corporate Information & Significant Accounting Policies")
                        .noteTotal(BigDecimal.ZERO)
                        .lines(List.of(FinancialsSchedule3DTO.NoteLine.builder().particulars("Basis of accounting: Accrual & Historical Cost").currentPeriod(BigDecimal.ZERO).previousPeriod(BigDecimal.ZERO).build()))
                        .build(),
                FinancialsSchedule3DTO.Schedule3Note.builder()
                        .noteNumber("2")
                        .title("Revenue from Operations")
                        .noteTotal(pl.getRevenueFromOperations())
                        .lines(List.of(FinancialsSchedule3DTO.NoteLine.builder().particulars("Sale of goods & services").currentPeriod(pl.getRevenueFromOperations()).previousPeriod(BigDecimal.ZERO).build()))
                        .build()
        );

        FinancialsSchedule3DTO dto = FinancialsSchedule3DTO.builder()
                .entityName(name)
                .cin(cin)
                .pan(pan)
                .asOfDate(to != null ? to : LocalDate.now().toString())
                .balanceSheet(bs)
                .profitLoss(pl)
                .cashFlow(cf)
                .notes(notes)
                .build();

        return ApiResponse.ok(dto);
    }
}
