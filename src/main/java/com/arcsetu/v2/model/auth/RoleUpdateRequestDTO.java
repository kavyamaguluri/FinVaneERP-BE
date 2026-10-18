package com.arcsetu.v2.model.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleUpdateRequestDTO {
    @NotBlank(message = "Role is required (ADMIN, CLIENT_ADMIN, CLIENT_USER)")
    private String role;
}
