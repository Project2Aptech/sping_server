package org.example.spring_server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.spring_server.dto.SongCommentDTO;
import org.example.spring_server.security.CustomUserDetails;
import org.example.spring_server.service.SongCommentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/songs/{songId}/comments")
@RequiredArgsConstructor
public class SongCommentController {

    private final SongCommentService songCommentService;

    @GetMapping
    public ResponseEntity<Page<SongCommentDTO.CommentResponse>> getComments(
            @PathVariable Integer songId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(songCommentService.findBySong(songId, pageable));
    }

    @PostMapping
    public ResponseEntity<SongCommentDTO.CommentResponse> create(
            @PathVariable Integer songId,
            @Valid @RequestBody SongCommentDTO.CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(songCommentService.create(getUserId(userDetails), songId, request));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer songId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        songCommentService.delete(getUserId(userDetails), commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{commentId}/replies")
    public ResponseEntity<Page<SongCommentDTO.CommentResponse>> getReplies(
            @PathVariable Integer songId,
            @PathVariable Long commentId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(songCommentService.findReplies(commentId, pageable));
    }

    private Integer getUserId(UserDetails userDetails) {
        return CustomUserDetails.extractId(userDetails);
    }
}