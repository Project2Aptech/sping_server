package org.example.spring_server.service;

import lombok.RequiredArgsConstructor;
import org.example.spring_server.dto.AlbumDTO;
import org.example.spring_server.entity.Album;
import org.example.spring_server.entity.User;
import org.example.spring_server.exception.DuplicateResourceException;
import org.example.spring_server.exception.ResourceNotFoundException;
import org.example.spring_server.mapper.AlbumMapper;
import org.example.spring_server.repository.AlbumRepository;
import org.example.spring_server.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;
    private final AlbumMapper albumMapper;

    @Transactional(readOnly = true)
    public AlbumDTO.AlbumResponse findById(Integer id) {
        return albumRepository.findById(id)
                .map(albumMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<AlbumDTO.AlbumResponse> findByArtist(Integer artistId, Pageable pageable) {
        return albumRepository.findByArtistId(artistId, pageable)
                .map(albumMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AlbumDTO.AlbumSummaryResponse> findByTitle(String title, Pageable pageable) {
        return albumRepository.findByTitleContainingIgnoreCase(title, pageable)
                .map(albumMapper::toSummaryResponse);
    }

    public AlbumDTO.AlbumResponse create(AlbumDTO.AlbumRequest request, Integer currentUserId) {
        if (!request.artistId().equals(currentUserId))
            throw new AccessDeniedException("You can only create albums for your own account");

        if (albumRepository.existsByArtistIdAndTitle(request.artistId(), request.title()))
            throw new DuplicateResourceException("Album title already exists for this artist");

        User artist = userRepository.findById(request.artistId())
                .orElseThrow(() -> new ResourceNotFoundException("Artist not found: " + request.artistId()));

        Album album = albumMapper.toEntity(request);
        album.setArtist(artist);

        return albumMapper.toResponse(albumRepository.save(album));
    }

    public void delete(Integer id, Integer currentUserId) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found: " + id));

        if (currentUserId != null && !album.getArtist().getId().equals(currentUserId))
            throw new AccessDeniedException("You can only delete your own albums");

        albumRepository.deleteById(id);
    }
}