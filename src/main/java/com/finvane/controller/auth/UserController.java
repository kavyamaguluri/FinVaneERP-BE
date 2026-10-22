package com.finvane.controller.auth;

import com.finvane.dao.auth.IUserJpaAccess;
import com.finvane.entity.auth.UserEntity;
import com.finvane.model.auth.RoleUpdateRequestDTO;
import com.finvane.model.auth.UserSummaryDTO;
import com.finvane.model.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserJpaAccess userDao;

    @GetMapping
    public ApiResponse<List<UserSummaryDTO>> getAllUsers() {
        List<UserEntity> users = userDao.findAll();
        List<UserSummaryDTO> dtos = users.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ApiResponse.ok(dtos);
    }

    @PutMapping("/{id}/role")
    public ApiResponse<UserSummaryDTO> updateUserRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequestDTO dto) {
        String newRole = dto.getRole().trim().toUpperCase();
        if (!List.of("ADMIN", "CLIENT_ADMIN", "CLIENT_USER").contains(newRole)) {
            throw new IllegalArgumentException("Invalid role: " + newRole + ". Permitted roles: ADMIN, CLIENT_ADMIN, CLIENT_USER");
        }

        UserEntity user = userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + id + " not found"));

        user.setRole(newRole);
        UserEntity saved = userDao.save(user);

        return ApiResponse.ok("User role updated successfully to " + newRole, toDTO(saved));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<UserSummaryDTO> toggleUserStatus(@PathVariable Long id) {
        UserEntity user = userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + id + " not found"));

        user.setIsActive(!Boolean.TRUE.equals(user.getIsActive()));
        UserEntity saved = userDao.save(user);

        String statusStr = Boolean.TRUE.equals(saved.getIsActive()) ? "activated" : "deactivated";
        return ApiResponse.ok("User " + statusStr + " successfully", toDTO(saved));
    }

    private UserSummaryDTO toDTO(UserEntity entity) {
        return UserSummaryDTO.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .fullName(entity.getFullName())
                .role(entity.getRole())
                .tenantId(entity.getTenantId())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
