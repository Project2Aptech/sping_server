package org.example.spring_server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GenreDTO {

    public record GenreResponse(
            Integer id,
            String name,
            String slug
    ) {}

    public record GenreRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Size(max = 100) String slug
    ) {}

    public record GenreSummaryResponse(
            Integer id,
            String name,
            String slug
    ) {}
}