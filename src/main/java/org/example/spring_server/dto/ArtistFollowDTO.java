package org.example.spring_server.dto;

import java.time.LocalDateTime;

public class ArtistFollowDTO {

    public record ArtistFollowResponse(
            Integer artistId,
            String artistName,
            String avatarUrl,
            LocalDateTime followedAt
    ) {}

    public record FollowerResponse(
            Integer userId,
            String displayName,
            String avatarUrl,
            LocalDateTime followedAt
    ) {}
}