package org.example.spring_server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.spring_server.enums.enumeration;

import java.time.LocalDateTime;

public class SongDTO {

    public record SongRequest(
            @NotNull Integer artistId,
            Integer albumId,
            @NotBlank String title,
            Integer durationSeconds,
            String coverUrl,
            Integer trackNumber,
            enumeration.AccountType requiredAccountType
    ) {}

    public record SongDetailResponse(
            Integer id,
            Integer artistId,
            Integer albumId,
            String title,
            Integer durationSeconds,
            String fileUrl,
            String coverUrl,
            Integer trackNumber,
            Long playCount,
            enumeration.SongStatus status,
            enumeration.AccountType requiredAccountType,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    public record SongSummaryResponse(
            Integer id,
            Integer artistId,
            String title,
            Integer durationSeconds,
            String coverUrl,
            Long playCount,
            enumeration.SongStatus status
    ) {}
}