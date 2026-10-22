package com.finvane.model.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private String tenantId;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
