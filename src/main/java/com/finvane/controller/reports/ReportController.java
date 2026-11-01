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
import org.springframework.jdbc.core.JdbcTemplate;
import java.math.RoundingMode;
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
    private final JdbcTemplate jdbcTemplate;

    private static class LedgerBalanceRecord {
        Long id;
        String code;
        String name;
        String nature;
        String primaryGroupName;
        String secondaryGroupName;
        BigDecimal openingBalance;
        BigDecimal totalDebit;
        BigDecimal totalCredit;
        BigDecimal balance; // totalDebit - totalCredit
    }

    private Map<Long, LedgerBalanceRecord> fetchPostedBalances(String tenantId, String startDate, String endDate) {
        String sql = """
            SELECT 
                al.id, al.code, al.name, ag.nature,
                COALESCE(pg.name, ag.name) AS primary_group_name,
                CASE WHEN ag.parent_id IS NOT NULL THEN ag.name ELSE NULL END AS secondary_group_name,
                COALESCE(al.opening_balance, 0) AS opening_balance,
                COALESCE(SUM(CASE WHEN v.id IS NOT NULL AND vi.entry_type = 'DEBIT' THEN vi.amount ELSE 0 END), 0) AS total_debit,
                COALESCE(SUM(CASE WHEN v.id IS NOT NULL AND vi.entry_type = 'CREDIT' THEN vi.amount ELSE 0 END), 0) AS total_credit
            FROM account_ledgers al
            JOIN account_groups ag ON ag.id = al.group_id
            LEFT JOIN account_groups pg ON pg.id = ag.parent_id
            LEFT JOIN voucher_items vi ON vi.ledger_id = al.id
            LEFT JOIN vouchers v ON v.id = vi.voucher_id AND v.tenant_id = ? AND v.voucher_date BETWEEN ? AND ?
            WHERE al.tenant_id = ? AND al.is_active = 1
            GROUP BY al.id, al.code, al.name, ag.nature, pg.name, ag.parent_id, ag.name, al.opening_balance
            ORDER BY al.code
        """;

        Map<Long, LedgerBalanceRecord> map = new LinkedHashMap<>();
        jdbcTemplate.query(sql, rs -> {
            LedgerBalanceRecord rec = new LedgerBalanceRecord();
            rec.id = rs.getLong("id");
            rec.code = rs.getString("code");
            rec.name = rs.getString("name");
            rec.nature = rs.getString("nature");
            rec.primaryGroupName = rs.getString("primary_group_name");
            rec.secondaryGroupName = rs.getString("secondary_group_name");
            rec.openingBalance = rs.getBigDecimal("opening_balance");
            rec.totalDebit = rs.getBigDecimal("total_debit");
            rec.totalCredit = rs.getBigDecimal("total_credit");
            rec.balance = rec.totalDebit.subtract(rec.totalCredit);
            map.put(rec.id, rec);
        }, tenantId, startDate, endDate, tenantId);

        return map;
    }

    @GetMapping("/trial-balance")
    public ApiResponse<TrialBalanceDTO> getTrialBalance(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String granularity) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";
        if (granularity == null || granularity.isBlank()) granularity = "custom";

        boolean dateRangeCorrected = false;
        if (from == null || from.isBlank()) from = "2024-04-01";
        if (to == null || to.isBlank()) to = LocalDate.now().toString();

        if (from.compareTo(to) > 0) {
            to = from;
            dateRangeCorrected = true;
        }

        // 1. Determine inception date
        String inceptionSql = "SELECT MIN(voucher_date) FROM vouchers WHERE tenant_id = ?";
        String inceptionDate = null;
        try {
            LocalDate incDate = jdbcTemplate.queryForObject(inceptionSql, LocalDate.class, tenantId);
            if (incDate != null) inceptionDate = incDate.toString();
        } catch (Exception ignored) {}
        if (inceptionDate == null) inceptionDate = "1900-01-01";

        // 2. Fetch period balances
        Map<Long, LedgerBalanceRecord> periodBalances = fetchPostedBalances(tenantId, from, to);

        // 3. Fetch prior balances if from > inception
        Map<Long, LedgerBalanceRecord> priorBalances = Collections.emptyMap();
        if (from.compareTo(inceptionDate) > 0) {
            LocalDate dayBefore = LocalDate.parse(from).minusDays(1);
            priorBalances = fetchPostedBalances(tenantId, inceptionDate, dayBefore.toString());
        }

        // 4. Sum manual opening balances on P&L accounts (Income/Expense) to fold into ledger 3900
        BigDecimal plOpeningAdjustment = BigDecimal.ZERO;
        String plSql = """
            SELECT COALESCE(SUM(al.opening_balance), 0)
            FROM account_ledgers al
            JOIN account_groups ag ON ag.id = al.group_id
            WHERE al.tenant_id = ? AND ag.nature IN ('INCOME', 'EXPENSE')
        """;
        try {
            BigDecimal plAdj = jdbcTemplate.queryForObject(plSql, BigDecimal.class, tenantId);
            if (plAdj != null) plOpeningAdjustment = plAdj;
        } catch (Exception ignored) {}

        List<TrialBalanceDTO.TrialBalanceRow> rows = new ArrayList<>();
        BigDecimal totOpDr = BigDecimal.ZERO;
        BigDecimal totOpCr = BigDecimal.ZERO;
        BigDecimal totDr = BigDecimal.ZERO;
        BigDecimal totCr = BigDecimal.ZERO;
        BigDecimal totClDr = BigDecimal.ZERO;
        BigDecimal totClCr = BigDecimal.ZERO;

        for (LedgerBalanceRecord r : periodBalances.values()) {
            boolean isPL = "INCOME".equalsIgnoreCase(r.nature) || "EXPENSE".equalsIgnoreCase(r.nature);
            BigDecimal priorBal = BigDecimal.ZERO;
            if (priorBalances.containsKey(r.id)) {
                priorBal = priorBalances.get(r.id).balance;
            }

            BigDecimal opBal = r.openingBalance != null ? r.openingBalance : BigDecimal.ZERO;
            BigDecimal opening = isPL ? BigDecimal.ZERO : opBal.add(priorBal);

            if ("3900".equals(r.code)) {
                opening = opening.add(plOpeningAdjustment);
            }

            BigDecimal periodBal = r.balance != null ? r.balance : BigDecimal.ZERO;
            BigDecimal closing = opening.add(periodBal);

            BigDecimal openingDr = opening.compareTo(BigDecimal.ZERO) > 0 ? opening : BigDecimal.ZERO;
            BigDecimal openingCr = opening.compareTo(BigDecimal.ZERO) < 0 ? opening.abs() : BigDecimal.ZERO;

            BigDecimal debit = periodBal.compareTo(BigDecimal.ZERO) > 0 ? periodBal : BigDecimal.ZERO;
            BigDecimal credit = periodBal.compareTo(BigDecimal.ZERO) < 0 ? periodBal.abs() : BigDecimal.ZERO;

            BigDecimal closingDr = closing.compareTo(BigDecimal.ZERO) > 0 ? closing : BigDecimal.ZERO;
            BigDecimal closingCr = closing.compareTo(BigDecimal.ZERO) < 0 ? closing.abs() : BigDecimal.ZERO;

            totOpDr = totOpDr.add(openingDr);
            totOpCr = totOpCr.add(openingCr);
            totDr = totDr.add(debit);
            totCr = totCr.add(credit);
            totClDr = totClDr.add(closingDr);
            totClCr = totClCr.add(closingCr);

            rows.add(TrialBalanceDTO.TrialBalanceRow.builder()
                    .id(r.id)
                    .code(r.code)
                    .name(r.name)
                    .primaryGroupName(r.primaryGroupName != null ? r.primaryGroupName : "General")
                    .secondaryGroupName(r.secondaryGroupName)
                    .nature(r.nature)
                    .type(r.nature != null ? r.nature.toLowerCase() : "")
                    .openingDr(openingDr.setScale(2, RoundingMode.HALF_UP))
                    .openingCr(openingCr.setScale(2, RoundingMode.HALF_UP))
                    .debit(debit.setScale(2, RoundingMode.HALF_UP))
                    .credit(credit.setScale(2, RoundingMode.HALF_UP))
                    .closingDr(closingDr.setScale(2, RoundingMode.HALF_UP))
                    .closingCr(closingCr.setScale(2, RoundingMode.HALF_UP))
                    .netBalance(closing.setScale(2, RoundingMode.HALF_UP))
                    .build());
        }

        TrialBalanceDTO.TrialBalanceTotals totals = TrialBalanceDTO.TrialBalanceTotals.builder()
                .openingDr(totOpDr.setScale(2, RoundingMode.HALF_UP))
                .openingCr(totOpCr.setScale(2, RoundingMode.HALF_UP))
                .debit(totDr.setScale(2, RoundingMode.HALF_UP))
                .credit(totCr.setScale(2, RoundingMode.HALF_UP))
                .closingDr(totClDr.setScale(2, RoundingMode.HALF_UP))
                .closingCr(totClCr.setScale(2, RoundingMode.HALF_UP))
                .build();

        // Also build groups for legacy compatibility
        Map<String, List<TrialBalanceDTO.TrialBalanceRow>> rowsByGroup = rows.stream()
                .collect(Collectors.groupingBy(TrialBalanceDTO.TrialBalanceRow::getPrimaryGroupName, LinkedHashMap::new, Collectors.toList()));

        List<TrialBalanceDTO.TrialBalanceGroup> groupDTOs = new ArrayList<>();
        for (Map.Entry<String, List<TrialBalanceDTO.TrialBalanceRow>> e : rowsByGroup.entrySet()) {
            BigDecimal grpD = e.getValue().stream().map(TrialBalanceDTO.TrialBalanceRow::getDebit).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal grpC = e.getValue().stream().map(TrialBalanceDTO.TrialBalanceRow::getCredit).reduce(BigDecimal.ZERO, BigDecimal::add);
            groupDTOs.add(TrialBalanceDTO.TrialBalanceGroup.builder()
                    .groupName(e.getKey())
                    .nature(e.getValue().isEmpty() ? "" : e.getValue().get(0).getNature())
                    .totalDebit(grpD)
                    .totalCredit(grpC)
                    .accounts(e.getValue())
                    .build());
        }

        TrialBalanceDTO dto = TrialBalanceDTO.builder()
                .fromDate(from)
                .toDate(to)
                .granularity(granularity)
                .dateRangeCorrected(dateRangeCorrected)
                .totalDebit(totDr.setScale(2, RoundingMode.HALF_UP))
                .totalCredit(totCr.setScale(2, RoundingMode.HALF_UP))
                .isBalanced(totClDr.compareTo(totClCr) == 0)
                .rows(rows)
                .totals(totals)
                .groups(groupDTOs)
                .build();

        return ApiResponse.ok(dto);
    }

    @GetMapping("/profit-loss")
    public ApiResponse<ProfitLossDTO> getProfitLoss(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";
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
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";
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
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";
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
        if (tenantId == null || tenantId.isBlank()) tenantId = "1";
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
