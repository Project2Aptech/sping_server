package org.example.spring_server.dto;


import java.time.LocalDateTime;

public class LikedSongDTO {

    public record LikedSongResponse(
            Integer songId,
            String title,
            String coverUrl,
            Integer artistId,
            String artistName,
            LocalDateTime likedAt
    ) {}
}