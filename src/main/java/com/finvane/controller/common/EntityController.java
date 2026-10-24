package com.finvane.controller.common;

import com.finvane.dao.common.IAuditLogJpaAccess;
import com.finvane.dao.common.IEntityJpaAccess;
import com.finvane.entity.common.AuditLogEntity;
import com.finvane.entity.common.EntityEntity;
import com.finvane.model.common.ApiResponse;
import com.finvane.model.common.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/entities")
@RequiredArgsConstructor
public class EntityController {

    private final IEntityJpaAccess entityDao;
    private final IAuditLogJpaAccess auditLogDao;

    @GetMapping("/active")
    public ApiResponse<EntityEntity> getActiveEntity() {
        String tenantId = TenantContext.getTenantId();
        EntityEntity entity = entityDao.findByTenantIdAndIsActiveTrue(tenantId)
                .orElse(null);
        if (entity == null) {
            // fallback to first active entity
            List<EntityEntity> list = entityDao.findByIsActiveTrueOrderByNameAsc();
            if (!list.isEmpty()) {
                entity = list.get(0);
            }
        }
        return ApiResponse.ok(entity);
    }

    @GetMapping
    public ApiResponse<List<EntityEntity>> getEntities() {
        return ApiResponse.ok(entityDao.findByIsActiveTrueOrderByNameAsc());
    }

    @GetMapping("/{id}")
    public ApiResponse<EntityEntity> getEntityById(@PathVariable Long id) {
        return entityDao.findById(id)
                .map(ApiResponse::ok)
                .orElse(ApiResponse.error("Business entity not found"));
    }

    @PostMapping
    public ApiResponse<EntityEntity> createEntity(@RequestBody EntityEntity entity) {
        if (entity.getTenantId() == null || entity.getTenantId().isBlank() || "1".equals(entity.getTenantId())) {
            entity.setTenantId(String.valueOf(System.currentTimeMillis()));
        }
        entity.setIsActive(true);
        EntityEntity saved = entityDao.save(entity);

        auditLogDao.save(AuditLogEntity.builder()
                .entityName(saved.getName())
                .action("create")
                .entity("entity")
                .entityId(String.valueOf(saved.getId()))
                .build());

        return ApiResponse.ok("Business entity created successfully", saved);
    }

    @PutMapping("/{id}")
    public ApiResponse<EntityEntity> updateEntity(@PathVariable Long id, @RequestBody EntityEntity req) {
        return entityDao.findById(id).map(existing -> {
            existing.setName(req.getName());
            existing.setLegalName(req.getLegalName());
            existing.setSector(req.getSector());
            existing.setBusinessType(req.getBusinessType());
            existing.setPan(req.getPan());
            existing.setGstRegistered(req.getGstRegistered());
            existing.setGstin(req.getGstin());
            existing.setConstitution(req.getConstitution());
            existing.setAddress(req.getAddress());
            existing.setStateCode(req.getStateCode());
            existing.setCountry(req.getCountry());
            existing.setBankName(req.getBankName());
            existing.setBankAccountName(req.getBankAccountName());
            existing.setBankAccountNo(req.getBankAccountNo());
            existing.setBankIfsc(req.getBankIfsc());
            existing.setBankBranch(req.getBankBranch());
            existing.setUpiId(req.getUpiId());
            existing.setDateOfCommencement(req.getDateOfCommencement());
            existing.setBooksStartDate(req.getBooksStartDate());
            existing.setFyStartMonth(req.getFyStartMonth());
            EntityEntity updated = entityDao.save(existing);

            auditLogDao.save(AuditLogEntity.builder()
                    .entityName(updated.getName())
                    .action("update")
                    .entity("entity")
                    .entityId(String.valueOf(updated.getId()))
                    .build());

            return ApiResponse.ok("Business entity updated successfully", updated);
        }).orElse(ApiResponse.error("Business entity not found"));
    }

    @PostMapping("/switch/{id}")
    public ApiResponse<EntityEntity> switchEntity(@PathVariable Long id) {
        return entityDao.findById(id).map(entity -> {
            auditLogDao.save(AuditLogEntity.builder()
                    .entityName(entity.getName())
                    .action("switch_entity")
                    .entity("entity")
                    .entityId(String.valueOf(entity.getId()))
                    .build());

            return ApiResponse.ok("Switched to " + entity.getName(), entity);
        }).orElse(ApiResponse.error("Business entity not found"));
    }
}
