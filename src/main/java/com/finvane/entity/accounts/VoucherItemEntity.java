package com.finvane.entity.accounts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "voucher_items", indexes = {
    @Index(name = "idx_item_voucher", columnList = "voucher_id"),
    @Index(name = "idx_item_ledger", columnList = "ledger_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    @JsonIgnore
    private VoucherEntity voucher;

    @Column(name = "ledger_id", nullable = false)
    private Long ledgerId;

    @Column(name = "entry_type", nullable = false, length = 16)
    private String entryType; // DEBIT or CREDIT

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "line_narration", length = 255)
    private String lineNarration;
}
