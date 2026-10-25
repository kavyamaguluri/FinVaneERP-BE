package com.finvane.controller.orders;

import com.finvane.dao.common.IPartyJpaAccess;
import com.finvane.dao.inventory.IItemJpaAccess;
import com.finvane.dao.orders.IQuotationJpaAccess;
import com.finvane.dao.orders.IQuotationLineJpaAccess;
import com.finvane.entity.common.PartyEntity;
import com.finvane.entity.inventory.ItemEntity;
import com.finvane.entity.orders.QuotationEntity;
import com.finvane.entity.orders.QuotationLineEntity;
import com.finvane.model.common.ApiResponse;
import com.finvane.model.common.TenantContext;
import com.finvane.model.orders.QuotationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/quotations")
@RequiredArgsConstructor
public class QuotationController {

    private final IQuotationJpaAccess quotationDao;
    private final IQuotationLineJpaAccess quotationLineDao;
    private final IPartyJpaAccess partyDao;
    private final IItemJpaAccess itemDao;

    @GetMapping
    public ApiResponse<List<QuotationDTO>> getQuotations() {
        String tenantId = TenantContext.getTenantId();
        List<QuotationEntity> list = quotationDao.findByTenantIdOrderByIdDesc(tenantId);
        Map<Long, PartyEntity> partyMap = partyDao.findByTenantIdAndIsActiveTrueOrderByNameAsc(tenantId).stream()
                .collect(Collectors.toMap(PartyEntity::getId, p -> p, (a, b) -> a));

        List<QuotationDTO> dtos = list.stream().map(q -> {
            PartyEntity p = partyMap.get(q.getPartyId());
            return QuotationDTO.builder()
                    .id(q.getId())
                    .quoteNo(q.getQuoteNo())
                    .partyId(q.getPartyId())
                    .partyName(p != null ? p.getName() : "Party #" + q.getPartyId())
                    .partyGstin(p != null ? p.getGstin() : "")
                    .quoteDate(q.getQuoteDate())
                    .validUntil(q.getValidUntil())
                    .taxableValue(q.getTaxableValue())
                    .cgst(q.getCgst())
                    .sgst(q.getSgst())
                    .igst(q.getIgst())
                    .totalAmount(q.getTotalAmount())
                    .status(q.getStatus())
                    .terms(q.getTerms())
                    .build();
        }).toList();

        return ApiResponse.ok(dtos);
    }

    @GetMapping("/{id}")
    public ApiResponse<QuotationDTO> getQuotationById(@PathVariable Long id) {
        String tenantId = TenantContext.getTenantId();
        QuotationEntity q = quotationDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quotation not found with ID: " + id));

        PartyEntity p = partyDao.findById(q.getPartyId()).orElse(null);
        List<QuotationLineEntity> lines = quotationLineDao.findByTenantIdAndQuotationId(tenantId, id);
        Map<Long, ItemEntity> itemMap = itemDao.findByTenantIdOrderByNameAsc(tenantId).stream()
                .collect(Collectors.toMap(ItemEntity::getId, it -> it, (a, b) -> a));

        List<QuotationDTO.QuotationLineDTO> lineDTOs = lines.stream().map(l -> {
            ItemEntity item = itemMap.get(l.getItemId());
            return QuotationDTO.QuotationLineDTO.builder()
                    .id(l.getId())
                    .quotationId(l.getQuotationId())
                    .itemId(l.getItemId())
                    .itemName(item != null ? item.getName() : "Item #" + l.getItemId())
                    .qty(l.getQty())
                    .rate(l.getRate())
                    .taxableValue(l.getTaxableValue())
                    .gstRate(l.getGstRate())
                    .cgst(l.getCgst())
                    .sgst(l.getSgst())
                    .igst(l.getIgst())
                    .amount(l.getAmount())
                    .build();
        }).toList();

        QuotationDTO dto = QuotationDTO.builder()
                .id(q.getId())
                .quoteNo(q.getQuoteNo())
                .partyId(q.getPartyId())
                .partyName(p != null ? p.getName() : "")
                .partyGstin(p != null ? p.getGstin() : "")
                .quoteDate(q.getQuoteDate())
                .validUntil(q.getValidUntil())
                .taxableValue(q.getTaxableValue())
                .cgst(q.getCgst())
                .sgst(q.getSgst())
                .igst(q.getIgst())
                .totalAmount(q.getTotalAmount())
                .status(q.getStatus())
                .terms(q.getTerms())
                .lines(lineDTOs)
                .build();

        return ApiResponse.ok(dto);
    }

    @PostMapping
    @Transactional
    public ApiResponse<QuotationDTO> createQuotation(@RequestBody QuotationDTO dto) {
        String tenantId = TenantContext.getTenantId();
        long count = quotationDao.countByTenantId(tenantId);
        int currentYear = LocalDate.now().getYear();
        String quoteNo = String.format("QT/%02d-%02d/%04d", currentYear % 100, (currentYear + 1) % 100, count + 1);

        PartyEntity party = partyDao.findById(dto.getPartyId())
                .orElseThrow(() -> new IllegalArgumentException("Party not found with ID: " + dto.getPartyId()));

        BigDecimal totalTaxable = BigDecimal.ZERO;
        BigDecimal totalCgst = BigDecimal.ZERO;
        BigDecimal totalSgst = BigDecimal.ZERO;
        BigDecimal totalIgst = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        List<QuotationLineEntity> linesToSave = new ArrayList<>();
        if (dto.getLines() != null) {
            for (QuotationDTO.QuotationLineDTO l : dto.getLines()) {
                if (l.getItemId() == null || l.getQty() == null || l.getQty().compareTo(BigDecimal.ZERO) <= 0) continue;
                ItemEntity item = itemDao.findById(l.getItemId()).orElse(null);
                BigDecimal rate = l.getRate() != null ? l.getRate() : (item != null && item.getSalePrice() != null ? item.getSalePrice() : BigDecimal.ZERO);
                BigDecimal taxable = l.getQty().multiply(rate).setScale(2, RoundingMode.HALF_UP);
                BigDecimal gstRate = l.getGstRate() != null ? l.getGstRate() : (item != null && item.getGstRate() != null ? item.getGstRate() : BigDecimal.valueOf(18));

                BigDecimal cgst = BigDecimal.ZERO;
                BigDecimal sgst = BigDecimal.ZERO;
                BigDecimal igst = BigDecimal.ZERO;

                // Intra-state default (27)
                boolean isIntra = party.getStateCode() == null || "27".equals(party.getStateCode());
                if (isIntra) {
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

                linesToSave.add(QuotationLineEntity.builder()
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
                        .build());
            }
        }

        QuotationEntity entity = QuotationEntity.builder()
                .tenantId(tenantId)
                .quoteNo(quoteNo)
                .partyId(party.getId())
                .quoteDate(dto.getQuoteDate() != null ? dto.getQuoteDate() : LocalDate.now().toString())
                .validUntil(dto.getValidUntil())
                .taxableValue(totalTaxable)
                .cgst(totalCgst)
                .sgst(totalSgst)
                .igst(totalIgst)
                .totalAmount(totalAmount)
                .status("draft")
                .terms(dto.getTerms())
                .build();
        QuotationEntity saved = quotationDao.save(entity);

        for (QuotationLineEntity line : linesToSave) {
            line.setQuotationId(saved.getId());
        }
        quotationLineDao.saveAll(linesToSave);

        dto.setId(saved.getId());
        dto.setQuoteNo(quoteNo);
        dto.setTaxableValue(totalTaxable);
        dto.setCgst(totalCgst);
        dto.setSgst(totalSgst);
        dto.setIgst(totalIgst);
        dto.setTotalAmount(totalAmount);
        dto.setStatus("draft");

        return ApiResponse.ok("Quotation created successfully", dto);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> deleteQuotation(@PathVariable Long id) {
        String tenantId = TenantContext.getTenantId();
        quotationLineDao.deleteByTenantIdAndQuotationId(tenantId, id);
        quotationDao.deleteById(id);
        return ApiResponse.ok("Quotation deleted successfully", null);
    }
}
