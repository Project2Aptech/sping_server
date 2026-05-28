package org.example.spring_server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.spring_server.dto.AlbumDTO;
import org.example.spring_server.security.CustomUserDetails;
import org.example.spring_server.service.AlbumService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    @GetMapping("/{id}")
    public ResponseEntity<AlbumDTO.AlbumResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(albumService.findById(id));
    }

    @GetMapping("/artist/{artistId}")
    public ResponseEntity<Page<AlbumDTO.AlbumResponse>> getByArtist(
            @PathVariable Integer artistId,
            @PageableDefault(size = 20, sort = "releaseDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(albumService.findByArtist(artistId, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<AlbumDTO.AlbumSummaryResponse>> searchByTitle(
            @RequestParam String title,
            Pageable pageable) {
        return ResponseEntity.ok(albumService.findByTitle(title, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ARTIST', 'ADMIN')")
    public ResponseEntity<AlbumDTO.AlbumResponse> create(
            @Valid @RequestBody AlbumDTO.AlbumRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Integer currentUserId = CustomUserDetails.extractId(userDetails);
        boolean isAdmin = CustomUserDetails.isAdmin(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(albumService.create(request, isAdmin ? request.artistId() : currentUserId));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ARTIST', 'ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Integer currentUserId = CustomUserDetails.extractId(userDetails);
        boolean isAdmin = CustomUserDetails.isAdmin(userDetails);
        albumService.delete(id, isAdmin ? null : currentUserId);
        return ResponseEntity.noContent().build();
    }
}