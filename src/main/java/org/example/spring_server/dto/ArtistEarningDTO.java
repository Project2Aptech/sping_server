package org.example.spring_server.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ArtistEarningDTO {

    public record ArtistEarningResponse(
            Integer id,
            Integer artistId,
            LocalDate periodStart,
            LocalDate periodEnd,
            Integer streamCount,
            BigDecimal amount,
            LocalDateTime createdAt
    ) {}

    public record ArtistEarningSummary(
            BigDecimal totalAmount,
            Integer totalStreams
    ) {}
}
