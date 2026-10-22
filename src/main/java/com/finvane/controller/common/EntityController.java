package com.finvane.controller.common;

import com.finvane.dao.common.IEntityJpaAccess;
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

    @GetMapping("/active")
    public ApiResponse<EntityEntity> getActiveEntity() {
        String tenantId = TenantContext.getTenantId();
        EntityEntity entity = entityDao.findByTenantIdAndIsActiveTrue(tenantId)
                .orElse(null);
        return ApiResponse.ok(entity);
    }

    @GetMapping
    public ApiResponse<List<EntityEntity>> getEntities() {
        return ApiResponse.ok(entityDao.findByIsActiveTrueOrderByNameAsc());
    }
}
