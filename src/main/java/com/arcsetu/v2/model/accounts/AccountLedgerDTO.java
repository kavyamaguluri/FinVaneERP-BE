package com.arcsetu.v2.model.accounts;

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
public class AccountLedgerDTO {

    private Long id;

    @NotBlank(message = "Ledger code is required")
    private String code;

    @NotBlank(message = "Ledger name is required")
    private String name;

    @NotNull(message = "Primary Group ID is required")
    private Long groupId;
    private String groupName;
    private String groupNature;

    private Long secondaryGroupId;
    private String secondaryGroupName;

    private BigDecimal openingBalance;
    private BigDecimal currentBalance;
    private String sectorReportLine;
    private String description;
    private Boolean isActive;
}
