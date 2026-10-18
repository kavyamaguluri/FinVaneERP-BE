package com.arcsetu.v2.controller.dashboard;

import com.arcsetu.v2.dao.accounts.IAccountGroupJpaAccess;
import com.arcsetu.v2.dao.accounts.IAccountLedgerJpaAccess;
import com.arcsetu.v2.dao.accounts.IVoucherJpaAccess;
import com.arcsetu.v2.dao.common.IPartyJpaAccess;
import com.arcsetu.v2.entity.accounts.AccountGroupEntity;
import com.arcsetu.v2.entity.accounts.AccountLedgerEntity;
import com.arcsetu.v2.entity.common.PartyEntity;
import com.arcsetu.v2.model.common.ApiResponse;
import com.arcsetu.v2.model.common.TenantContext;
import com.arcsetu.v2.model.dashboard.DashboardMetricsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final IAccountLedgerJpaAccess ledgerDao;
    private final IAccountGroupJpaAccess groupDao;
    private final IPartyJpaAccess partyDao;
    private final IVoucherJpaAccess voucherDao;

    @GetMapping("/metrics")
    public ApiResponse<DashboardMetricsDTO> getMetrics() {
        String tenantId = TenantContext.getTenantId();

        List<AccountLedgerEntity> ledgers = ledgerDao.findByTenantIdAndIsActiveTrueOrderByNameAsc(tenantId);
        List<AccountGroupEntity> groups = groupDao.findByTenantIdOrderByNameAsc(tenantId);
        List<PartyEntity> parties = partyDao.findByTenantIdAndIsActiveTrueOrderByNameAsc(tenantId);

        Map<Long, AccountGroupEntity> groupMap = groups.stream()
                .collect(Collectors.toMap(AccountGroupEntity::getId, g -> g, (a, b) -> a));

        BigDecimal cashBankBalance = BigDecimal.ZERO;
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (AccountLedgerEntity l : ledgers) {
            BigDecimal bal = l.getCurrentBalance() != null ? l.getCurrentBalance() : BigDecimal.ZERO;
            AccountGroupEntity grp = groupMap.get(l.getGroupId());
            if (grp != null) {
                String nature = grp.getNature();
                String nameLower = l.getName().toLowerCase();
                String codeLower = l.getCode().toLowerCase();

                if ("ASSET".equalsIgnoreCase(nature) && (nameLower.contains("cash") || nameLower.contains("bank") || codeLower.startsWith("cash") || codeLower.startsWith("bank"))) {
                    cashBankBalance = cashBankBalance.add(bal);
                } else if ("INCOME".equalsIgnoreCase(nature)) {
                    totalIncome = totalIncome.add(bal);
                } else if ("EXPENSE".equalsIgnoreCase(nature)) {
                    totalExpense = totalExpense.add(bal);
                }
            }
        }

        BigDecimal netProfit = totalIncome.subtract(totalExpense);

        BigDecimal totalReceivables = BigDecimal.ZERO;
        BigDecimal totalPayables = BigDecimal.ZERO;

        for (PartyEntity p : parties) {
            BigDecimal bal = p.getCurrentBalance() != null ? p.getCurrentBalance() : BigDecimal.ZERO;
            if ("CUSTOMER".equalsIgnoreCase(p.getPartyType())) {
                totalReceivables = totalReceivables.add(bal);
            } else if ("VENDOR".equalsIgnoreCase(p.getPartyType())) {
                totalPayables = totalPayables.add(bal);
            } else if ("BOTH".equalsIgnoreCase(p.getPartyType())) {
                if (bal.compareTo(BigDecimal.ZERO) >= 0) {
                    totalReceivables = totalReceivables.add(bal);
                } else {
                    totalPayables = totalPayables.add(bal.abs());
                }
            }
        }

        List<String> months = List.of("Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "Jan", "Feb", "Mar");
        List<BigDecimal> monthlySales = new ArrayList<>(Collections.nCopies(12, BigDecimal.ZERO));
        List<BigDecimal> monthlyExpenses = new ArrayList<>(Collections.nCopies(12, BigDecimal.ZERO));

        DashboardMetricsDTO dto = DashboardMetricsDTO.builder()
                .cashBankBalance(cashBankBalance)
                .totalReceivables(totalReceivables)
                .totalPayables(totalPayables)
                .netProfit(netProfit)
                .totalLedgers(ledgers.size())
                .totalParties(parties.size())
                .totalVouchers(voucherDao.count())
                .chartMonths(months)
                .monthlySales(monthlySales)
                .monthlyExpenses(monthlyExpenses)
                .build();

        return ApiResponse.ok(dto);
    }
}
