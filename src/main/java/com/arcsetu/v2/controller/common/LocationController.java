package com.arcsetu.v2.controller.common;

import com.arcsetu.v2.dao.common.ILocationJpaAccess;
import com.arcsetu.v2.entity.common.LocationEntity;
import com.arcsetu.v2.model.common.ApiResponse;
import com.arcsetu.v2.model.common.TenantContext;
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
