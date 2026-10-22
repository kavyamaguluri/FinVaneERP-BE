package com.finvane.model.common;

public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = ThreadLocal.withInitial(() -> "1");

    private TenantContext() {}

    public static void setTenantId(String tenantId) {
        if (tenantId != null && !tenantId.isBlank()) {
            CURRENT_TENANT.set(tenantId.trim());
        } else {
            CURRENT_TENANT.set("1");
        }
    }

    public static String getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
