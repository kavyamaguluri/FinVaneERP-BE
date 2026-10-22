package com.finvane.service.accounts;

import com.finvane.dao.accounts.IAccountGroupJpaAccess;
import com.finvane.dao.accounts.IAccountLedgerJpaAccess;
import com.finvane.dao.accounts.IVoucherItemJpaAccess;
import com.finvane.entity.accounts.AccountGroupEntity;
import com.finvane.entity.accounts.AccountLedgerEntity;
import com.finvane.entity.accounts.VoucherEntity;
import com.finvane.entity.accounts.VoucherItemEntity;
import com.finvane.model.accounts.AccountLedgerDTO;
import com.finvane.model.accounts.LedgerStatementDTO;
import com.finvane.model.accounts.TrialBalanceRowDTO;
import com.finvane.model.common.PageResponse;
import com.finvane.model.common.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LedgerServiceImpl implements ILedgerService {

    private final IAccountLedgerJpaAccess ledgerDao;
    private final IAccountGroupJpaAccess groupDao;
    private final IVoucherItemJpaAccess voucherItemDao;

    @Override
    @Transactional
    public AccountLedgerDTO createLedger(AccountLedgerDTO dto) {
        String tenantId = TenantContext.getTenantId();

        if (ledgerDao.findByTenantIdAndCode(tenantId, dto.getCode()).isPresent()) {
            throw new IllegalArgumentException("Ledger with code '" + dto.getCode() + "' already exists");
        }

        AccountGroupEntity group = groupDao.findById(dto.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("Account group not found with ID: " + dto.getGroupId()));

        BigDecimal opening = dto.getOpeningBalance() != null ? dto.getOpeningBalance() : BigDecimal.ZERO;

        AccountLedgerEntity entity = AccountLedgerEntity.builder()
                .tenantId(tenantId)
                .code(dto.getCode().trim())
                .name(dto.getName().trim())
                .groupId(group.getId())
                .secondaryGroupId(dto.getSecondaryGroupId())
                .openingBalance(opening)
                .currentBalance(opening)
                .sectorReportLine(dto.getSectorReportLine())
                .description(dto.getDescription())
                .isActive(true)
                .build();

        AccountLedgerEntity saved = ledgerDao.save(entity);
        return mapToDTO(saved, group);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountLedgerDTO getLedgerById(Long id) {
        AccountLedgerEntity entity = ledgerDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ledger not found with ID: " + id));
        AccountGroupEntity group = groupDao.findById(entity.getGroupId()).orElse(null);
        return mapToDTO(entity, group);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountLedgerDTO> getAllActiveLedgers() {
        String tenantId = TenantContext.getTenantId();
        return ledgerDao.findByTenantIdAndIsActiveTrueOrderByNameAsc(tenantId)
                .stream()
                .map(l -> mapToDTO(l, groupDao.findById(l.getGroupId()).orElse(null)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AccountLedgerDTO> searchLedgers(String search, Long groupId, int page, int size) {
        String tenantId = TenantContext.getTenantId();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<AccountLedgerEntity> result = ledgerDao.searchLedgers(tenantId, (search != null && !search.isBlank()) ? search.trim() : null, groupId, pageRequest);

        List<AccountLedgerDTO> dtos = result.getContent().stream()
                .map(l -> mapToDTO(l, groupDao.findById(l.getGroupId()).orElse(null)))
                .toList();

        return PageResponse.<AccountLedgerDTO>builder()
                .content(dtos)
                .pageNumber(result.getNumber())
                .pageSize(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LedgerStatementDTO getLedgerStatement(Long ledgerId, LocalDate fromDate, LocalDate toDate) {
        String tenantId = TenantContext.getTenantId();
        AccountLedgerEntity ledger = ledgerDao.findById(ledgerId)
                .orElseThrow(() -> new IllegalArgumentException("Ledger not found with ID: " + ledgerId));
        AccountGroupEntity group = groupDao.findById(ledger.getGroupId()).orElse(null);

        List<VoucherItemEntity> items = voucherItemDao.findLedgerTransactions(tenantId, ledgerId, fromDate, toDate);

        BigDecimal runningBalance = ledger.getOpeningBalance();
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        List<LedgerStatementDTO.TransactionRow> rows = new ArrayList<>();

        for (VoucherItemEntity item : items) {
            VoucherEntity v = item.getVoucher();
            boolean isDebit = "DEBIT".equalsIgnoreCase(item.getEntryType());
            BigDecimal amt = item.getAmount();

            BigDecimal debitAmt = isDebit ? amt : BigDecimal.ZERO;
            BigDecimal creditAmt = !isDebit ? amt : BigDecimal.ZERO;

            totalDebit = totalDebit.add(debitAmt);
            totalCredit = totalCredit.add(creditAmt);

            String nature = group != null ? group.getNature() : "ASSET";
            if ("ASSET".equalsIgnoreCase(nature) || "EXPENSE".equalsIgnoreCase(nature)) {
                runningBalance = isDebit ? runningBalance.add(amt) : runningBalance.subtract(amt);
            } else {
                runningBalance = isDebit ? runningBalance.subtract(amt) : runningBalance.add(amt);
            }

            rows.add(LedgerStatementDTO.TransactionRow.builder()
                    .voucherId(v.getId())
                    .voucherNumber(v.getVoucherNumber())
                    .voucherType(v.getVoucherType())
                    .date(v.getVoucherDate())
                    .particulars(item.getLineNarration() != null ? item.getLineNarration() : v.getNarration())
                    .debit(debitAmt)
                    .credit(creditAmt)
                    .runningBalance(runningBalance)
                    .build());
        }

        return LedgerStatementDTO.builder()
                .ledgerId(ledger.getId())
                .ledgerCode(ledger.getCode())
                .ledgerName(ledger.getName())
                .groupName(group != null ? group.getName() : "")
                .fromDate(fromDate)
                .toDate(toDate)
                .openingBalance(ledger.getOpeningBalance())
                .transactions(rows)
                .totalDebit(totalDebit)
                .totalCredit(totalCredit)
                .closingBalance(runningBalance)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TrialBalanceRowDTO.Report getTrialBalance(LocalDate asOfDate) {
        String tenantId = TenantContext.getTenantId();
        List<AccountLedgerEntity> ledgers = ledgerDao.findByTenantIdAndIsActiveTrueOrderByNameAsc(tenantId);

        List<TrialBalanceRowDTO.Row> rows = new ArrayList<>();
        BigDecimal grandDebit = BigDecimal.ZERO;
        BigDecimal grandCredit = BigDecimal.ZERO;

        for (AccountLedgerEntity l : ledgers) {
            AccountGroupEntity g = groupDao.findById(l.getGroupId()).orElse(null);
            String nature = g != null ? g.getNature() : "ASSET";
            BigDecimal bal = l.getCurrentBalance();

            BigDecimal debit = BigDecimal.ZERO;
            BigDecimal credit = BigDecimal.ZERO;

            if ("ASSET".equalsIgnoreCase(nature) || "EXPENSE".equalsIgnoreCase(nature)) {
                if (bal.compareTo(BigDecimal.ZERO) >= 0) {
                    debit = bal;
                } else {
                    credit = bal.abs();
                }
            } else {
                if (bal.compareTo(BigDecimal.ZERO) >= 0) {
                    credit = bal;
                } else {
                    debit = bal.abs();
                }
            }

            grandDebit = grandDebit.add(debit);
            grandCredit = grandCredit.add(credit);

            rows.add(TrialBalanceRowDTO.Row.builder()
                    .ledgerId(l.getId())
                    .ledgerCode(l.getCode())
                    .ledgerName(l.getName())
                    .groupName(g != null ? g.getName() : "")
                    .nature(nature)
                    .closingDebit(debit)
                    .closingCredit(credit)
                    .build());
        }

        boolean balanced = grandDebit.compareTo(grandCredit) == 0;

        return TrialBalanceRowDTO.Report.builder()
                .asOfDate(asOfDate != null ? asOfDate : LocalDate.now())
                .rows(rows)
                .totalDebit(grandDebit)
                .totalCredit(grandCredit)
                .isBalanced(balanced)
                .build();
    }

    private AccountLedgerDTO mapToDTO(AccountLedgerEntity l, AccountGroupEntity g) {
        return AccountLedgerDTO.builder()
                .id(l.getId())
                .code(l.getCode())
                .name(l.getName())
                .groupId(l.getGroupId())
                .groupName(g != null ? g.getName() : "")
                .groupNature(g != null ? g.getNature() : "")
                .secondaryGroupId(l.getSecondaryGroupId())
                .openingBalance(l.getOpeningBalance())
                .currentBalance(l.getCurrentBalance())
                .sectorReportLine(l.getSectorReportLine())
                .description(l.getDescription())
                .isActive(l.getIsActive())
                .build();
    }
}
