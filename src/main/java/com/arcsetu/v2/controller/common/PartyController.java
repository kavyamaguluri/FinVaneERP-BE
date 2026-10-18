package com.arcsetu.v2.controller.common;

import com.arcsetu.v2.dao.common.IPartyJpaAccess;
import com.arcsetu.v2.entity.common.PartyEntity;
import com.arcsetu.v2.model.common.ApiResponse;
import com.arcsetu.v2.model.common.PageResponse;
import com.arcsetu.v2.model.common.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/parties")
@RequiredArgsConstructor
public class PartyController {

    private final IPartyJpaAccess partyDao;

    @GetMapping
    public ApiResponse<PageResponse<PartyEntity>> getParties(
            @RequestParam(required = false) String partyType,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String tenantId = TenantContext.getTenantId();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<PartyEntity> result = partyDao.searchParties(tenantId, partyType, search, pageRequest);

        return ApiResponse.ok(PageResponse.<PartyEntity>builder()
                .content(result.getContent())
                .pageNumber(result.getNumber())
                .pageSize(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build());
    }

    @GetMapping("/active")
    public ApiResponse<List<PartyEntity>> getActiveParties(@RequestParam(required = false) String partyType) {
        String tenantId = TenantContext.getTenantId();
        if (partyType != null && !partyType.isBlank()) {
            return ApiResponse.ok(partyDao.findByTenantIdAndPartyTypeAndIsActiveTrueOrderByNameAsc(tenantId, partyType.toUpperCase()));
        }
        return ApiResponse.ok(partyDao.findByTenantIdAndIsActiveTrueOrderByNameAsc(tenantId));
    }

    @PostMapping
    public ApiResponse<PartyEntity> createParty(@Valid @RequestBody PartyEntity party) {
        party.setTenantId(TenantContext.getTenantId());
        return ApiResponse.ok("Party saved successfully", partyDao.save(party));
    }

    @GetMapping("/{id}")
    public ApiResponse<PartyEntity> getPartyById(@PathVariable Long id) {
        PartyEntity entity = partyDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Party not found with ID: " + id));
        return ApiResponse.ok(entity);
    }
}
