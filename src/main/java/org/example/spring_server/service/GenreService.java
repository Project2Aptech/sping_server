package org.example.spring_server.service;

import lombok.RequiredArgsConstructor;
import org.example.spring_server.dto.GenreDTO;
import org.example.spring_server.entity.Genre;
import org.example.spring_server.exception.DuplicateResourceException;
import org.example.spring_server.exception.ResourceNotFoundException;
import org.example.spring_server.mapper.GenreMapper;
import org.example.spring_server.repository.GenreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GenreService {

    private final GenreRepository genreRepository;
    private final GenreMapper genreMapper;

    @Transactional(readOnly = true)
    public List<GenreDTO.GenreResponse> findAll() {
        return genreRepository.findAll()
                .stream()
                .map(genreMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public GenreDTO.GenreResponse findById(Integer id) {
        return genreRepository.findById(id)
                .map(genreMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found: " + id));
    }

    public GenreDTO.GenreResponse create(GenreDTO.GenreRequest request) {
        if (genreRepository.existsByName(request.name()))
            throw new DuplicateResourceException("Genre name already exists");
        if (genreRepository.existsBySlug(request.slug()))
            throw new DuplicateResourceException("Genre slug already exists");

        Genre genre = genreMapper.toEntity(request);
        return genreMapper.toResponse(genreRepository.save(genre));
    }

    public void delete(Integer id) {
        if (!genreRepository.existsById(id))
            throw new ResourceNotFoundException("Genre not found: " + id);
        genreRepository.deleteById(id);
    }
}