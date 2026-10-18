package com.arcsetu.v2.model.accounts;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherRequestDTO {

    private Long id;

    private String voucherNumber;

    @NotBlank(message = "Voucher type is required (JOURNAL, PAYMENT, RECEIPT, CONTRA)")
    private String voucherType;

    @NotNull(message = "Voucher date is required")
    private LocalDate voucherDate;

    private String narration;
    private String referenceNo;

    @NotEmpty(message = "Voucher must contain at least two entries")
    @Valid
    private List<VoucherItemDTO> items;
}
