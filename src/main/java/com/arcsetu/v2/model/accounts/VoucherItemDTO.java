package com.arcsetu.v2.model.accounts;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherItemDTO {

    private Long id;

    @NotNull(message = "Ledger ID is required")
    private Long ledgerId;

    private String ledgerName;
    private String ledgerCode;

    @NotBlank(message = "Entry type must be DEBIT or CREDIT")
    private String entryType; // DEBIT or CREDIT

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    private String lineNarration;
}
