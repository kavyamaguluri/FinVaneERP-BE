package com.finvane.controller.invoices;

import com.finvane.dao.accounts.IAccountLedgerJpaAccess;
import com.finvane.dao.accounts.IVoucherItemJpaAccess;
import com.finvane.dao.accounts.IVoucherJpaAccess;
import com.finvane.dao.common.IEntityJpaAccess;
import com.finvane.dao.common.ILocationJpaAccess;
import com.finvane.dao.common.IPartyJpaAccess;
import com.finvane.dao.inventory.IItemJpaAccess;
import com.finvane.dao.invoices.IInvoiceJpaAccess;
import com.finvane.dao.invoices.IInvoiceLineJpaAccess;
import com.finvane.entity.accounts.AccountLedgerEntity;
import com.finvane.entity.accounts.VoucherEntity;
import com.finvane.entity.accounts.VoucherItemEntity;
import com.finvane.entity.common.EntityEntity;
import com.finvane.entity.common.LocationEntity;
import com.finvane.entity.common.PartyEntity;
import com.finvane.entity.inventory.ItemEntity;
import com.finvane.entity.invoices.InvoiceEntity;
import com.finvane.entity.invoices.InvoiceLineEntity;
import com.finvane.model.common.ApiResponse;
import com.finvane.model.common.TenantContext;
import com.finvane.model.invoices.InvoiceDTO;
import com.finvane.model.invoices.InvoiceLineDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final IInvoiceJpaAccess invoiceDao;
    private final IInvoiceLineJpaAccess invoiceLineDao;
    private final IPartyJpaAccess partyDao;
    private final ILocationJpaAccess locationDao;
    private final IItemJpaAccess itemDao;
    private final IVoucherJpaAccess voucherDao;
    private final IVoucherItemJpaAccess voucherItemDao;
    private final IAccountLedgerJpaAccess ledgerDao;
    private final IEntityJpaAccess entityDao;

    @GetMapping
    public ApiResponse<List<InvoiceDTO>> getInvoices(@RequestParam(required = false, defaultValue = "sales") String type) {
        String tenantId = TenantContext.getTenantId();
        List<InvoiceEntity> list = invoiceDao.findByTenantIdAndInvoiceTypeOrderByIdDesc(tenantId, type.toLowerCase());

        Map<Long, PartyEntity> partyMap = partyDao.findByTenantIdAndIsActiveTrueOrderByNameAsc(tenantId).stream()
                .collect(Collectors.toMap(PartyEntity::getId, p -> p, (a, b) -> a));
        Map<Long, LocationEntity> locMap = locationDao.findByTenantIdAndIsActiveTrueOrderByNameAsc(tenantId).stream()
                .collect(Collectors.toMap(LocationEntity::getId, l -> l, (a, b) -> a));

        List<InvoiceDTO> dtos = list.stream().map(i -> {
            PartyEntity p = partyMap.get(i.getPartyId());
            LocationEntity l = locMap.get(i.getLocationId());
            return InvoiceDTO.builder()
                    .id(i.getId())
                    .invoiceNo(i.getInvoiceNo())
                    .invoiceType(i.getInvoiceType())
                    .partyId(i.getPartyId())
                    .partyName(p != null ? p.getName() : "Party #" + i.getPartyId())
                    .partyGstin(p != null ? p.getGstin() : "")
                    .locationId(i.getLocationId())
                    .locationCode(l != null ? l.getCode() : "")
                    .invoiceDate(i.getInvoiceDate())
                    .dueDate(i.getDueDate())
                    .placeOfSupply(i.getPlaceOfSupply())
                    .taxableValue(i.getTaxableValue())
                    .cgst(i.getCgst())
                    .sgst(i.getSgst())
                    .igst(i.getIgst())
                    .totalAmount(i.getTotalAmount())
                    .status(i.getStatus())
                    .voucherId(i.getVoucherId())
                    .irn(i.getIrn())
                    .irnAckNo(i.getIrnAckNo())
                    .irnAckDate(i.getIrnAckDate())
                    .irnQr(i.getIrnQr())
                    .ewbNo(i.getEwbNo())
                    .build();
        }).toList();

        return ApiResponse.ok(dtos);
    }

    @GetMapping("/{id}")
    public ApiResponse<InvoiceDTO> getInvoiceById(@PathVariable Long id) {
        String tenantId = TenantContext.getTenantId();
        InvoiceEntity i = invoiceDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + id));

        PartyEntity p = partyDao.findById(i.getPartyId()).orElse(null);
        LocationEntity l = i.getLocationId() != null ? locationDao.findById(i.getLocationId()).orElse(null) : null;
        List<InvoiceLineEntity> lines = invoiceLineDao.findByTenantIdAndInvoiceId(tenantId, id);
        Map<Long, ItemEntity> itemMap = itemDao.findByTenantIdOrderByNameAsc(tenantId).stream()
                .collect(Collectors.toMap(ItemEntity::getId, it -> it, (a, b) -> a));

        List<InvoiceLineDTO> lineDTOs = lines.stream().map(line -> {
            ItemEntity item = itemMap.get(line.getItemId());
            return InvoiceLineDTO.builder()
                    .id(line.getId())
                    .invoiceId(line.getInvoiceId())
                    .itemId(line.getItemId())
                    .itemName(item != null ? item.getName() : "Item #" + line.getItemId())
                    .itemType(item != null ? item.getItemType() : "")
                    .hsnSac(line.getHsnSac() != null ? line.getHsnSac() : (item != null ? item.getHsnSac() : ""))
                    .qty(line.getQty())
                    .rate(line.getRate())
                    .taxableValue(line.getTaxableValue())
                    .gstRate(line.getGstRate())
                    .cgst(line.getCgst())
                    .sgst(line.getSgst())
                    .igst(line.getIgst())
                    .amount(line.getAmount())
                    .description(line.getDescription())
                    .build();
        }).toList();

        InvoiceDTO dto = InvoiceDTO.builder()
                .id(i.getId())
                .invoiceNo(i.getInvoiceNo())
                .invoiceType(i.getInvoiceType())
                .partyId(i.getPartyId())
                .partyName(p != null ? p.getName() : "")
                .partyGstin(p != null ? p.getGstin() : "")
                .locationId(i.getLocationId())
                .locationCode(l != null ? l.getCode() : "")
                .invoiceDate(i.getInvoiceDate())
                .dueDate(i.getDueDate())
                .placeOfSupply(i.getPlaceOfSupply())
                .taxableValue(i.getTaxableValue())
                .cgst(i.getCgst())
                .sgst(i.getSgst())
                .igst(i.getIgst())
                .totalAmount(i.getTotalAmount())
                .status(i.getStatus())
                .voucherId(i.getVoucherId())
                .irn(i.getIrn())
                .irnAckNo(i.getIrnAckNo())
                .irnAckDate(i.getIrnAckDate())
                .irnQr(i.getIrnQr())
                .ewbNo(i.getEwbNo())
                .lines(lineDTOs)
                .build();

        return ApiResponse.ok(dto);
    }

    @PostMapping
    @Transactional
    public ApiResponse<InvoiceDTO> createInvoice(@RequestBody InvoiceDTO dto) {
        String tenantId = TenantContext.getTenantId();
        String type = (dto.getInvoiceType() != null && dto.getInvoiceType().equalsIgnoreCase("purchase")) ? "purchase" : "sales";

        PartyEntity party = partyDao.findById(dto.getPartyId())
                .orElseThrow(() -> new IllegalArgumentException("Party not found with ID: " + dto.getPartyId()));

        long count = invoiceDao.countByTenantIdAndInvoiceType(tenantId, type);
        String prefix = "sales".equals(type) ? "SI" : "PI";
        String invoiceNo = String.format("%s-%05d", prefix, count + 1);

        String homeState = "27"; // Default Maharashtra state code
        Optional<EntityEntity> curEntity = entityDao.findById(Long.parseLong(tenantId));
        if (curEntity.isPresent() && curEntity.get().getStateCode() != null) {
            homeState = curEntity.get().getStateCode();
        }

        String pos = dto.getPlaceOfSupply();
        if (pos == null || pos.isBlank()) {
            pos = party.getStateCode() != null ? party.getStateCode() : homeState;
        }

        BigDecimal totalTaxable = BigDecimal.ZERO;
        BigDecimal totalCgst = BigDecimal.ZERO;
        BigDecimal totalSgst = BigDecimal.ZERO;
        BigDecimal totalIgst = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        List<InvoiceLineEntity> computedLines = new ArrayList<>();
        if (dto.getLines() != null) {
            for (InvoiceLineDTO l : dto.getLines()) {
                if (l.getItemId() == null || l.getQty() == null || l.getQty().compareTo(BigDecimal.ZERO) <= 0) continue;
                ItemEntity item = itemDao.findById(l.getItemId()).orElse(null);
                BigDecimal rate = l.getRate() != null ? l.getRate() : (item != null && item.getSalePrice() != null ? item.getSalePrice() : BigDecimal.ZERO);
                BigDecimal taxable = l.getQty().multiply(rate).setScale(2, RoundingMode.HALF_UP);
                BigDecimal gstRate = l.getGstRate() != null ? l.getGstRate() : (item != null && item.getGstRate() != null ? item.getGstRate() : BigDecimal.valueOf(18));

                BigDecimal cgst = BigDecimal.ZERO;
                BigDecimal sgst = BigDecimal.ZERO;
                BigDecimal igst = BigDecimal.ZERO;

                boolean isIntraState = homeState.equalsIgnoreCase(pos);
                if (isIntraState) {
                    BigDecimal halfRate = gstRate.divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
                    cgst = taxable.multiply(halfRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    sgst = taxable.multiply(halfRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                } else {
                    igst = taxable.multiply(gstRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                }
                BigDecimal lineTotal = taxable.add(cgst).add(sgst).add(igst);

                totalTaxable = totalTaxable.add(taxable);
                totalCgst = totalCgst.add(cgst);
                totalSgst = totalSgst.add(sgst);
                totalIgst = totalIgst.add(igst);
                totalAmount = totalAmount.add(lineTotal);

                computedLines.add(InvoiceLineEntity.builder()
                        .tenantId(tenantId)
                        .itemId(l.getItemId())
                        .qty(l.getQty())
                        .rate(rate)
                        .taxableValue(taxable)
                        .gstRate(gstRate)
                        .cgst(cgst)
                        .sgst(sgst)
                        .igst(igst)
                        .amount(lineTotal)
                        .hsnSac(item != null ? item.getHsnSac() : "")
                        .build());
            }
        }

        // Post auto-journal voucher
        String voucherNo = String.format("%s-JV-%s", prefix, invoiceNo);
        VoucherEntity v = VoucherEntity.builder()
                .tenantId(tenantId)
                .voucherNumber(voucherNo)
                .voucherDate(dto.getInvoiceDate() != null ? LocalDate.parse(dto.getInvoiceDate()) : LocalDate.now())
                .voucherType("INVOICE")
                .narration("Auto-posted from invoice " + invoiceNo)
                .totalAmount(totalAmount)
                .build();
        VoucherEntity savedVoucher = voucherDao.save(v);

        AccountLedgerEntity debtors = getLedgerByCode(tenantId, "1200");
        AccountLedgerEntity creditors = getLedgerByCode(tenantId, "2100");
        AccountLedgerEntity salesAcc = getLedgerByCode(tenantId, "4000");
        AccountLedgerEntity purchaseAcc = getLedgerByCode(tenantId, "5000");
        AccountLedgerEntity outCgst = getLedgerByCode(tenantId, "2200");
        AccountLedgerEntity outSgst = getLedgerByCode(tenantId, "2201");
        AccountLedgerEntity outIgst = getLedgerByCode(tenantId, "2202");
        AccountLedgerEntity inCgst = getLedgerByCode(tenantId, "1400");
        AccountLedgerEntity inSgst = getLedgerByCode(tenantId, "1401");
        AccountLedgerEntity inIgst = getLedgerByCode(tenantId, "1402");

        List<VoucherItemEntity> vItems = new ArrayList<>();
        if ("sales".equals(type)) {
            if (debtors != null) vItems.add(VoucherItemEntity.builder().voucher(savedVoucher).ledgerId(debtors.getId()).entryType("DEBIT").amount(totalAmount).lineNarration("Debtor balance").build());
            if (salesAcc != null) vItems.add(VoucherItemEntity.builder().voucher(savedVoucher).ledgerId(salesAcc.getId()).entryType("CREDIT").amount(totalTaxable).lineNarration("Sales Revenue").build());
            if (outCgst != null && totalCgst.compareTo(BigDecimal.ZERO) > 0) vItems.add(VoucherItemEntity.builder().voucher(savedVoucher).ledgerId(outCgst.getId()).entryType("CREDIT").amount(totalCgst).lineNarration("Output CGST").build());
            if (outSgst != null && totalSgst.compareTo(BigDecimal.ZERO) > 0) vItems.add(VoucherItemEntity.builder().voucher(savedVoucher).ledgerId(outSgst.getId()).entryType("CREDIT").amount(totalSgst).lineNarration("Output SGST").build());
            if (outIgst != null && totalIgst.compareTo(BigDecimal.ZERO) > 0) vItems.add(VoucherItemEntity.builder().voucher(savedVoucher).ledgerId(outIgst.getId()).entryType("CREDIT").amount(totalIgst).lineNarration("Output IGST").build());
        } else {
            if (purchaseAcc != null) vItems.add(VoucherItemEntity.builder().voucher(savedVoucher).ledgerId(purchaseAcc.getId()).entryType("DEBIT").amount(totalTaxable).lineNarration("Purchase Expense").build());
            if (inCgst != null && totalCgst.compareTo(BigDecimal.ZERO) > 0) vItems.add(VoucherItemEntity.builder().voucher(savedVoucher).ledgerId(inCgst.getId()).entryType("DEBIT").amount(totalCgst).lineNarration("Input CGST").build());
            if (inSgst != null && totalSgst.compareTo(BigDecimal.ZERO) > 0) vItems.add(VoucherItemEntity.builder().voucher(savedVoucher).ledgerId(inSgst.getId()).entryType("DEBIT").amount(totalSgst).lineNarration("Input SGST").build());
            if (inIgst != null && totalIgst.compareTo(BigDecimal.ZERO) > 0) vItems.add(VoucherItemEntity.builder().voucher(savedVoucher).ledgerId(inIgst.getId()).entryType("DEBIT").amount(totalIgst).lineNarration("Input IGST").build());
            if (creditors != null) vItems.add(VoucherItemEntity.builder().voucher(savedVoucher).ledgerId(creditors.getId()).entryType("CREDIT").amount(totalAmount).lineNarration("Creditor balance").build());
        }
        voucherItemDao.saveAll(vItems);

        InvoiceEntity invoice = InvoiceEntity.builder()
                .tenantId(tenantId)
                .invoiceNo(invoiceNo)
                .invoiceType(type)
                .partyId(party.getId())
                .locationId(dto.getLocationId())
                .invoiceDate(dto.getInvoiceDate() != null ? dto.getInvoiceDate() : LocalDate.now().toString())
                .dueDate(dto.getDueDate() != null ? dto.getDueDate() : LocalDate.now().toString())
                .placeOfSupply(pos)
                .taxableValue(totalTaxable)
                .cgst(totalCgst)
                .sgst(totalSgst)
                .igst(totalIgst)
                .totalAmount(totalAmount)
                .status("posted")
                .voucherId(savedVoucher.getId())
                .build();
        InvoiceEntity savedInvoice = invoiceDao.save(invoice);

        for (InvoiceLineEntity line : computedLines) {
            line.setInvoiceId(savedInvoice.getId());
        }
        invoiceLineDao.saveAll(computedLines);

        dto.setId(savedInvoice.getId());
        dto.setInvoiceNo(invoiceNo);
        dto.setTaxableValue(totalTaxable);
        dto.setCgst(totalCgst);
        dto.setSgst(totalSgst);
        dto.setIgst(totalIgst);
        dto.setTotalAmount(totalAmount);
        dto.setStatus("posted");
        dto.setVoucherId(savedVoucher.getId());

        return ApiResponse.ok("Invoice created successfully", dto);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> deleteInvoice(@PathVariable Long id) {
        String tenantId = TenantContext.getTenantId();
        InvoiceEntity entity = invoiceDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + id));

        if (entity.getVoucherId() != null) {
            voucherDao.deleteById(entity.getVoucherId());
        }
        invoiceLineDao.deleteByTenantIdAndInvoiceId(tenantId, id);
        invoiceDao.deleteById(id);
        return ApiResponse.ok("Invoice deleted successfully", null);
    }

    private AccountLedgerEntity getLedgerByCode(String tenantId, String code) {
        return ledgerDao.findByTenantIdAndIsActiveTrueOrderByNameAsc(tenantId).stream()
                .filter(l -> code.equals(l.getCode()))
                .findFirst()
                .orElse(null);
    }
}
