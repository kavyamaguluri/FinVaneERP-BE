package com.finvane.controller.auth;

import com.finvane.dao.auth.IUserJpaAccess;
import com.finvane.entity.auth.UserEntity;
import com.finvane.model.auth.AuthResponseDTO;
import com.finvane.model.auth.LoginRequestDTO;
import com.finvane.model.auth.RegisterRequestDTO;
import com.finvane.model.common.ApiResponse;
import com.finvane.utils.JwtUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IUserJpaAccess userDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtility jwtUtility;

    @PostMapping("/register")
    public ApiResponse<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        String username = dto.getUsername();
        String email = dto.getEmail();

        if (userDao.existsByUsername(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already registered");
        }

        if (userDao.existsByEmail(email)) {
            throw new IllegalArgumentException("Email '" + email + "' is already registered");
        }

        // 3 Roles: ADMIN (Platform Admin / granted by Admin), CLIENT_ADMIN, CLIENT_USER
        String role = "CLIENT_USER";
        String configuredAdminEmail = System.getenv("ADMIN_EMAIL");
        if (configuredAdminEmail != null && configuredAdminEmail.equals(email)) {
            role = "ADMIN";
        } else if (dto.getRole() != null && "CLIENT_ADMIN".equalsIgnoreCase(dto.getRole())) {
            role = "CLIENT_ADMIN";
        } else if (dto.getRole() != null && ("CLIENT_USER".equalsIgnoreCase(dto.getRole()) || "USER".equalsIgnoreCase(dto.getRole()))) {
            role = "CLIENT_USER";
        } else if (dto.getRole() != null && "ADMIN".equalsIgnoreCase(dto.getRole())) {
            role = "CLIENT_ADMIN";
        }

        String tenantId = (dto.getTenantId() != null && !dto.getTenantId().isBlank()) ? dto.getTenantId() : "1";

        UserEntity user = UserEntity.builder()
                .username(username)
                .email(email)
                .fullName(dto.getFullName())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(role)
                .tenantId(tenantId)
                .isActive(true)
                .build();

        UserEntity savedUser = userDao.save(user);

        String token = jwtUtility.generateToken(savedUser.getUsername(), savedUser.getRole(), savedUser.getTenantId());

        AuthResponseDTO response = AuthResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole())
                .tenantId(savedUser.getTenantId())
                .build();

        return ApiResponse.ok("User registered successfully as " + role, response);
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        String identifier = dto.getUsername();

        UserEntity user = userDao.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new IllegalStateException("Your account has been deactivated. Please contact your administrator.");
        }

        String token = jwtUtility.generateToken(user.getUsername(), user.getRole(), user.getTenantId());

        AuthResponseDTO response = AuthResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .tenantId(user.getTenantId())
                .build();

        return ApiResponse.ok("Login successful", response);
    }
}
