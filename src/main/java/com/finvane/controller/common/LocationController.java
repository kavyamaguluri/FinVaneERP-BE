package com.finvane.controller.common;

import com.finvane.dao.common.ILocationJpaAccess;
import com.finvane.entity.common.LocationEntity;
import com.finvane.model.common.ApiResponse;
import com.finvane.model.common.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/locations")
@RequiredArgsConstructor
public class LocationController {

    private final ILocationJpaAccess locationDao;

    @GetMapping
    public ApiResponse<List<LocationEntity>> getLocations() {
        String tenantId = TenantContext.getTenantId();
        return ApiResponse.ok(locationDao.findByTenantIdAndIsActiveTrueOrderByNameAsc(tenantId));
    }

    @PostMapping
    public ApiResponse<LocationEntity> createLocation(@Valid @RequestBody LocationEntity location) {
        location.setTenantId(TenantContext.getTenantId());
        return ApiResponse.ok("Location saved successfully", locationDao.save(location));
    }

    @GetMapping("/{id}")
    public ApiResponse<LocationEntity> getLocationById(@PathVariable Long id) {
        LocationEntity entity = locationDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with ID: " + id));
        return ApiResponse.ok(entity);
    }
}
