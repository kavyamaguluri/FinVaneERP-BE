package com.finvane.entity.payroll;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    @Builder.Default
    private String tenantId = "1";

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "designation")
    private String designation;

    @Column(name = "gender")
    private String gender;

    @Column(name = "pan_enc")
    private String panEnc;

    @Column(name = "bank_account_enc")
    private String bankAccountEnc;

    @Column(name = "ifsc")
    private String ifsc;

    @Column(name = "monthly_ctc", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyCtc;

    @Column(name = "date_of_joining")
    private String dateOfJoining;

    @Column(name = "date_of_exit")
    private String dateOfExit;

    @Column(name = "is_active")
    @Builder.Default
    private Long isActive = 1L;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
