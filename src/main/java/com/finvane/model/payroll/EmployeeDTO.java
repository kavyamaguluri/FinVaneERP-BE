package com.finvane.model.payroll;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    private Long id;
    private String name;
    private String email;
    private String designation;
    private String gender;
    private String pan;
    private String panEnc;
    private String bankAccount;
    private String bankAccountEnc;
    private String bankMasked;
    private String ifsc;
    private BigDecimal monthlyCtc;
    private String dateOfJoining;
    private String dateOfExit;
    private Long isActive;
}
