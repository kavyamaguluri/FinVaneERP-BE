package com.arcsetu.v2.service.accounts;

import com.arcsetu.v2.model.accounts.VoucherRequestDTO;
import com.arcsetu.v2.model.common.PageResponse;

import java.time.LocalDate;

public interface IVoucherService {

    VoucherRequestDTO createVoucher(VoucherRequestDTO request);

    VoucherRequestDTO getVoucherById(Long id);

    PageResponse<VoucherRequestDTO> searchVouchers(String voucherType, LocalDate startDate, LocalDate endDate, int page, int size);
}
