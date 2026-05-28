package org.example.spring_server.dto;

import jakarta.validation.constraints.NotNull;
import org.example.spring_server.enums.enumeration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SubscriptionDTO {

    public record SubscriptionResponse(
            Integer id,
            Integer userId,
            enumeration.PlanType planType,
            BigDecimal amount,
            LocalDateTime startedAt,
            LocalDateTime expiresAt,
            enumeration.SubscriptionStatus status,
            LocalDateTime createdAt
    ) {}

    public record SubscriptionRequest(
            @NotNull enumeration.PlanType planType
    ) {}
}