package org.example.spring_server.controller;

import lombok.RequiredArgsConstructor;
import org.example.spring_server.dto.HistoryDTO;
import org.example.spring_server.security.CustomUserDetails;
import org.example.spring_server.service.HistoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService playHistoryService;

    @GetMapping
    public ResponseEntity<Page<HistoryDTO.HistoryResponse>> getMyHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(playHistoryService.findByUser(getUserId(userDetails), pageable));
    }

    @PostMapping("/songs/{songId}")
    public ResponseEntity<HistoryDTO.HistoryResponse> record(
            @PathVariable Integer songId,
            @RequestBody HistoryDTO.HistoryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                playHistoryService.record(getUserId(userDetails), songId, request));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        playHistoryService.clearHistory(getUserId(userDetails));
        return ResponseEntity.noContent().build();
    }

    private Integer getUserId(UserDetails userDetails) {
        return CustomUserDetails.extractId(userDetails);
    }
}