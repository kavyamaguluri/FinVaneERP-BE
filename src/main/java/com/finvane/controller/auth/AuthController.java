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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IUserJpaAccess userDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtility jwtUtility;

    @PostMapping("/register")
    public ApiResponse<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        String cleanUsername = dto.getUsername().trim().toLowerCase();
        String cleanEmail = dto.getEmail().trim().toLowerCase();

        if (userDao.existsByUsername(cleanUsername)) {
            throw new IllegalArgumentException("Username '" + cleanUsername + "' is already registered");
        }

        if (userDao.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("Email '" + cleanEmail + "' is already registered");
        }

        // 3 Roles: ADMIN (Platform Admin for kavyasony7512@gmail.com / granted by Admin), CLIENT_ADMIN, CLIENT_USER
        String role = "CLIENT_USER";
        if (cleanEmail.equalsIgnoreCase("kavyasony7512@gmail.com")) {
            role = "ADMIN";
        } else if (dto.getRole() != null && dto.getRole().trim().equalsIgnoreCase("CLIENT_ADMIN")) {
            role = "CLIENT_ADMIN";
        } else if (dto.getRole() != null && (dto.getRole().trim().equalsIgnoreCase("CLIENT_USER") || dto.getRole().trim().equalsIgnoreCase("USER"))) {
            role = "CLIENT_USER";
        } else if (dto.getRole() != null && dto.getRole().trim().equalsIgnoreCase("ADMIN")) {
            // Only primary admin or explicit grant allows ADMIN; others default to CLIENT_ADMIN
            role = "CLIENT_ADMIN";
        }

        String tenantId = (dto.getTenantId() != null && !dto.getTenantId().isBlank()) ? dto.getTenantId() : "1";

        UserEntity user = UserEntity.builder()
                .username(cleanUsername)
                .email(cleanEmail)
                .fullName(dto.getFullName().trim())
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
        String identifier = dto.getUsername().trim().toLowerCase();

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
