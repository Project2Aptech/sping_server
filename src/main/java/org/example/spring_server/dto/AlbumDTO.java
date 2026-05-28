package org.example.spring_server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AlbumDTO {

    public record AlbumResponse(
            Integer id,
            Integer artistId,
            String artistName,
            String title,
            String coverUrl,
            LocalDate releaseDate,
            LocalDateTime createdAt
    ) {}

    public record AlbumSummaryResponse(
            Integer id,
            String title,
            String coverUrl,
            LocalDate releaseDate
    ) {}

    public record AlbumRequest(
            @NotNull Integer artistId,
            @NotBlank @Size(max = 255) String title,
            String coverUrl,
            LocalDate releaseDate
    ) {}
}