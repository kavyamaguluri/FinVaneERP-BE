package com.finvane.controller.common;

import com.finvane.dao.common.IAuditLogJpaAccess;
import com.finvane.entity.common.AuditLogEntity;
import com.finvane.model.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final IAuditLogJpaAccess auditLogDao;

    @GetMapping
    public ApiResponse<List<AuditLogEntity>> getAuditLogs() {
        return ApiResponse.ok(auditLogDao.findAllByOrderByCreatedAtDesc());
    }
}
