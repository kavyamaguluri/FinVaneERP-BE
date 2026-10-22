package com.finvane.service.accounts;

import com.finvane.model.accounts.AccountLedgerDTO;
import com.finvane.model.accounts.LedgerStatementDTO;
import com.finvane.model.accounts.TrialBalanceRowDTO;
import com.finvane.model.common.PageResponse;

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
