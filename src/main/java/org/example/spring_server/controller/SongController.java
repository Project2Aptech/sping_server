package org.example.spring_server.controller;

import lombok.RequiredArgsConstructor;
import org.example.spring_server.dto.SongDTO;
import org.example.spring_server.security.CustomUserDetails;
import org.example.spring_server.service.SongService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    @GetMapping("/{id}")
    public ResponseEntity<SongDTO.SongDetailResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(songService.findById(id));
    }

    @GetMapping("/album/{albumId}")
    public ResponseEntity<Page<SongDTO.SongSummaryResponse>> getByAlbum(
            @PathVariable Integer albumId,
            @PageableDefault(size = 20, sort = "trackNumber") Pageable pageable) {
        return ResponseEntity.ok(songService.findByAlbum(albumId, pageable));
    }

    @GetMapping("/artist/{artistId}")
    public ResponseEntity<Page<SongDTO.SongSummaryResponse>> getByArtist(
            @PathVariable Integer artistId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(songService.findByArtist(artistId, pageable));
    }

    @GetMapping
    public ResponseEntity<Page<SongDTO.SongSummaryResponse>> getLive(
            @PageableDefault(size = 20, sort = "playCount", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(songService.findLive(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<SongDTO.SongSummaryResponse>> searchByTitle(
            @RequestParam String title,
            Pageable pageable) {
        return ResponseEntity.ok(songService.findByTitle(title, pageable));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ARTIST', 'ADMIN')")
    public ResponseEntity<SongDTO.SongDetailResponse> create(
            @RequestPart("data") SongDTO.SongRequest request,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        Integer currentUserId = CustomUserDetails.extractId(userDetails);
        if (userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"))) {
            return ResponseEntity.ok(songService.create(request, file, request.artistId()));
        }
        return ResponseEntity.ok(songService.create(request, file, currentUserId));
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAnyAuthority('ARTIST', 'ADMIN')")
    public ResponseEntity<SongDTO.SongDetailResponse> publish(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Integer currentUserId = CustomUserDetails.extractId(userDetails);
        boolean isAdmin = CustomUserDetails.isAdmin(userDetails);
        return ResponseEntity.ok(songService.publish(id, isAdmin ? null : currentUserId));
    }

    @PatchMapping("/{id}/playback")
    public ResponseEntity<SongDTO.SongDetailResponse> playback(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(songService.playback(id, CustomUserDetails.extractId(userDetails)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ARTIST', 'ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Integer currentUserId = CustomUserDetails.extractId(userDetails);
        boolean isAdmin = CustomUserDetails.isAdmin(userDetails);
        songService.delete(id, isAdmin ? null : currentUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/genre/{genreId}")
    public ResponseEntity<Page<SongDTO.SongSummaryResponse>> getByGenre(
            @PathVariable Integer genreId,
            @PageableDefault(size = 20, sort = "playCount", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(songService.findByGenre(genreId, pageable));
    }
}