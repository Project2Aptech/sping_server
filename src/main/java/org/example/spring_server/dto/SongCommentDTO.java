package org.example.spring_server.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class SongCommentDTO {

    public record CommentResponse(
            Long id,
            Integer songId,
            Integer userId,
            String username,
            Long parentCommentId,
            String content,
            boolean isDeleted,
            LocalDateTime createdAt
    ) {}

    public record CommentRequest(
            @NotBlank String content,
            Long parentCommentId
    ) {}
}