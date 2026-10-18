package com.arcsetu.v2.service.accounts;

import com.arcsetu.v2.model.accounts.AccountLedgerDTO;
import com.arcsetu.v2.model.accounts.LedgerStatementDTO;
import com.arcsetu.v2.model.accounts.TrialBalanceRowDTO;
import com.arcsetu.v2.model.common.PageResponse;

import java.time.LocalDate;
import java.util.List;

public interface ILedgerService {

    AccountLedgerDTO createLedger(AccountLedgerDTO dto);

    AccountLedgerDTO getLedgerById(Long id);

    List<AccountLedgerDTO> getAllActiveLedgers();

    PageResponse<AccountLedgerDTO> searchLedgers(String search, Long groupId, int page, int size);

    LedgerStatementDTO getLedgerStatement(Long ledgerId, LocalDate fromDate, LocalDate toDate);

    TrialBalanceRowDTO.Report getTrialBalance(LocalDate asOfDate);
}
