package org.example.spring_server.dto;

import org.example.spring_server.enums.enumeration;

import java.time.LocalDateTime;

public class HistoryDTO {
    public record HistoryResponse(
            Long id,
            Integer songId,
            String songTitle,
            String coverUrl,
            Integer secondsPlayed,
            enumeration.DeviceType deviceType,
            LocalDateTime playedAt
    ) {
    }

    public record HistoryRequest(
            Integer secondsPlayed,
            enumeration.DeviceType deviceType
    ) {
    }

}
