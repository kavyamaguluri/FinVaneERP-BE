package com.finvane.service.accounts;

import com.finvane.model.accounts.VoucherRequestDTO;
import com.finvane.model.common.PageResponse;

import java.time.LocalDate;

public interface IVoucherService {

    VoucherRequestDTO createVoucher(VoucherRequestDTO request);

    VoucherRequestDTO getVoucherById(Long id);

    PageResponse<VoucherRequestDTO> searchVouchers(String voucherType, LocalDate startDate, LocalDate endDate, int page, int size);
}
