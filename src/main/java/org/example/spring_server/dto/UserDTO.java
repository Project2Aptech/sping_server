package org.example.spring_server.dto;

import jakarta.validation.constraints.*;
import org.example.spring_server.enums.enumeration;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class UserDTO {

    public record RegisterRequest(
            @NotBlank @Size(max = 50) String username,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6) String password,
            String displayName,
            LocalDate birthDate
    ) {}

    public record AdminUpdateRequest(
            enumeration.UserRole role,
            enumeration.AccountType accountType
    ) {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    public record AuthResponse(
            String accessToken,
            UserSummary user
    ) {}

    public record UserSummary(
            Integer id,
            String username,
            String email,
            String displayName,
            String avatarUrl,
            LocalDate birthDate,
            enumeration.UserRole role,
            enumeration.AccountType accountType) {}

    public record UserDetailResponse(
            Integer id,
            String username,
            String email,
            String displayName,
            String avatarUrl,
            String bio,
            LocalDate birthDate,
            enumeration.UserRole role,
            enumeration.AccountType accountType,
            boolean isActive,
            LocalDateTime createdAt
    ) {}

    public record UserPublicResponse(
            Integer id,
            String username,
            String displayName,
            String avatarUrl,
            String bio,
            LocalDate birthDate
    ) {}

    public record UpdateRequest(
            String displayName,
            String bio,
            String avatarUrl,
            LocalDate birthDate
    ) {}

    public record ForgotPasswordRequest(
            @NotBlank @Email String email
    ) {}

    public record ResetPasswordRequest(
            @NotBlank String token,
            @NotBlank @Size(min = 6) String newPassword
    ) {}
}