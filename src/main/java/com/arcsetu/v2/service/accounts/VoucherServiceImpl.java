package com.arcsetu.v2.service.accounts;

import com.arcsetu.v2.dao.accounts.IAccountGroupJpaAccess;
import com.arcsetu.v2.dao.accounts.IAccountLedgerJpaAccess;
import com.arcsetu.v2.dao.accounts.IVoucherItemJpaAccess;
import com.arcsetu.v2.dao.accounts.IVoucherJpaAccess;
import com.arcsetu.v2.entity.accounts.AccountGroupEntity;
import com.arcsetu.v2.entity.accounts.AccountLedgerEntity;
import com.arcsetu.v2.entity.accounts.VoucherEntity;
import com.arcsetu.v2.entity.accounts.VoucherItemEntity;
import com.arcsetu.v2.model.accounts.VoucherItemDTO;
import com.arcsetu.v2.model.accounts.VoucherRequestDTO;
import com.arcsetu.v2.model.common.PageResponse;
import com.arcsetu.v2.model.common.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements IVoucherService {

    private final IVoucherJpaAccess voucherDao;
    private final IVoucherItemJpaAccess voucherItemDao;
    private final IAccountLedgerJpaAccess ledgerDao;
    private final IAccountGroupJpaAccess groupDao;

    @Override
    @Transactional
    public VoucherRequestDTO createVoucher(VoucherRequestDTO request) {
        String tenantId = TenantContext.getTenantId();

        if (request.getItems() == null || request.getItems().size() < 2) {
            throw new IllegalArgumentException("Voucher must have at least two line items (Debit and Credit)");
        }

        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (VoucherItemDTO item : request.getItems()) {
            if ("DEBIT".equalsIgnoreCase(item.getEntryType())) {
                totalDebit = totalDebit.add(item.getAmount());
            } else if ("CREDIT".equalsIgnoreCase(item.getEntryType())) {
                totalCredit = totalCredit.add(item.getAmount());
            } else {
                throw new IllegalArgumentException("Invalid entry type: " + item.getEntryType() + ". Must be DEBIT or CREDIT");
            }
        }

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new IllegalArgumentException(String.format("Double-entry accounting violation: Total Debit (₹%s) does not equal Total Credit (₹%s)",
                    totalDebit, totalCredit));
        }

        // Auto-generate voucher number if not provided
        String voucherNo = request.getVoucherNumber();
        if (voucherNo == null || voucherNo.isBlank()) {
            voucherNo = generateVoucherNumber(tenantId, request.getVoucherType(), request.getVoucherDate());
        }

        VoucherEntity voucher = VoucherEntity.builder()
                .tenantId(tenantId)
                .voucherNumber(voucherNo)
                .voucherType(request.getVoucherType().toUpperCase())
                .voucherDate(request.getVoucherDate())
                .narration(request.getNarration())
                .referenceNo(request.getReferenceNo())
                .totalAmount(totalDebit)
                .items(new ArrayList<>())
                .build();

        for (VoucherItemDTO itemDto : request.getItems()) {
            AccountLedgerEntity ledger = ledgerDao.findById(itemDto.getLedgerId())
                    .orElseThrow(() -> new IllegalArgumentException("Ledger not found with ID: " + itemDto.getLedgerId()));

            AccountGroupEntity group = groupDao.findById(ledger.getGroupId()).orElse(null);
            String nature = group != null ? group.getNature() : "ASSET";

            // Update ledger running balance
            BigDecimal amount = itemDto.getAmount();
            boolean isDebit = "DEBIT".equalsIgnoreCase(itemDto.getEntryType());

            // Asset & Expense increase on Debit, decrease on Credit.
            // Liability, Equity, Income increase on Credit, decrease on Debit.
            if ("ASSET".equalsIgnoreCase(nature) || "EXPENSE".equalsIgnoreCase(nature)) {
                ledger.setCurrentBalance(isDebit ? ledger.getCurrentBalance().add(amount) : ledger.getCurrentBalance().subtract(amount));
            } else {
                ledger.setCurrentBalance(isDebit ? ledger.getCurrentBalance().subtract(amount) : ledger.getCurrentBalance().add(amount));
            }
            ledgerDao.save(ledger);

            VoucherItemEntity itemEntity = VoucherItemEntity.builder()
                    .voucher(voucher)
                    .ledgerId(ledger.getId())
                    .entryType(itemDto.getEntryType().toUpperCase())
                    .amount(amount)
                    .lineNarration(itemDto.getLineNarration())
                    .build();

            voucher.getItems().add(itemEntity);
        }

        VoucherEntity saved = voucherDao.save(voucher);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherRequestDTO getVoucherById(Long id) {
        VoucherEntity voucher = voucherDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Voucher not found with ID: " + id));
        return mapToDTO(voucher);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VoucherRequestDTO> searchVouchers(String voucherType, LocalDate startDate, LocalDate endDate, int page, int size) {
        String tenantId = TenantContext.getTenantId();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "voucherDate", "id"));
        Page<VoucherEntity> result = voucherDao.searchVouchers(tenantId, voucherType, startDate, endDate, pageRequest);

        List<VoucherRequestDTO> dtos = result.getContent().stream().map(this::mapToDTO).toList();

        return PageResponse.<VoucherRequestDTO>builder()
                .content(dtos)
                .pageNumber(result.getNumber())
                .pageSize(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
    }

    private String generateVoucherNumber(String tenantId, String voucherType, LocalDate date) {
        String prefix = switch (voucherType.toUpperCase()) {
            case "PAYMENT" -> "PV";
            case "RECEIPT" -> "RV";
            case "CONTRA" -> "CV";
            default -> "JV";
        };

        // Financial year e.g. 26-27
        int year = date.getYear() % 100;
        int month = date.getMonthValue();
        String fy = (month >= 4) ? String.format("%02d-%02d", year, (year + 1) % 100)
                                 : String.format("%02d-%02d", (year - 1) % 100, year);

        long count = voucherDao.countByTenantIdAndVoucherType(tenantId, voucherType.toUpperCase()) + 1;
        return String.format("%s/%s/%04d", prefix, fy, count);
    }

    private VoucherRequestDTO mapToDTO(VoucherEntity v) {
        List<VoucherItemDTO> itemDtos = v.getItems().stream().map(item -> {
            AccountLedgerEntity l = ledgerDao.findById(item.getLedgerId()).orElse(null);
            return VoucherItemDTO.builder()
                    .id(item.getId())
                    .ledgerId(item.getLedgerId())
                    .ledgerName(l != null ? l.getName() : "Unknown Ledger")
                    .ledgerCode(l != null ? l.getCode() : "")
                    .entryType(item.getEntryType())
                    .amount(item.getAmount())
                    .lineNarration(item.getLineNarration())
                    .build();
        }).toList();

        return VoucherRequestDTO.builder()
                .id(v.getId())
                .voucherNumber(v.getVoucherNumber())
                .voucherType(v.getVoucherType())
                .voucherDate(v.getVoucherDate())
                .narration(v.getNarration())
                .referenceNo(v.getReferenceNo())
                .items(itemDtos)
                .build();
    }
}
