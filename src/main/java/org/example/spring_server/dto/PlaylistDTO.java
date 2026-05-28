package org.example.spring_server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class PlaylistDTO {

    public record PlaylistResponse(
            Integer id,
            Integer userId,
            String title,
            String description,
            String coverUrl,
            boolean isPublic,
            LocalDateTime createdAt
    ) {}

    public record PlaylistSummaryResponse(
            Integer id,
            String title,
            String coverUrl,
            boolean isPublic
    ) {}

    public record PlaylistRequest(
            @NotBlank @Size(max = 255) String title,
            String description,
            String coverUrl,
            boolean isPublic
    ) {}

    public record PlaylistUpdateRequest(
            String title,
            String description,
            String coverUrl,
            Boolean isPublic
    ) {}
}