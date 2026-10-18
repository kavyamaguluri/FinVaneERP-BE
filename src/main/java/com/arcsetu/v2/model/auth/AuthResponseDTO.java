package com.arcsetu.v2.model.auth;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {
    private String token;
    private String tokenType;
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String role; // ADMIN or USER
    private String tenantId;
}
