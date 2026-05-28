package org.example.spring_server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.spring_server.dto.GenreDTO;
import org.example.spring_server.service.GenreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @GetMapping
    public ResponseEntity<List<GenreDTO.GenreResponse>> getAll() {
        return ResponseEntity.ok(genreService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenreDTO.GenreResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(genreService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ARTIST', 'ADMIN')")
    public ResponseEntity<GenreDTO.GenreResponse> create(
            @Valid @RequestBody GenreDTO.GenreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(genreService.create(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ARTIST', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        genreService.delete(id);
        return ResponseEntity.noContent().build();
    }
}