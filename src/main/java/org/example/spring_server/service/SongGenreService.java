package org.example.spring_server.service;

import lombok.RequiredArgsConstructor;
import org.example.spring_server.dto.SongDTO;
import org.example.spring_server.entity.Genre;
import org.example.spring_server.entity.Song;
import org.example.spring_server.entity.SongGenre;
import org.example.spring_server.enums.enumeration;
import org.example.spring_server.exception.DuplicateResourceException;
import org.example.spring_server.exception.ResourceNotFoundException;
import org.example.spring_server.mapper.SongMapper;
import org.example.spring_server.repository.GenreRepository;
import org.example.spring_server.repository.SongGenreRepository;
import org.example.spring_server.repository.SongRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SongGenreService {

    private final SongGenreRepository songGenreRepository;
    private final SongRepository songRepository;
    private final GenreRepository genreRepository;
    private final SongMapper songMapper;

    @Transactional(readOnly = true)
    public Page<SongDTO.SongSummaryResponse> findSongsByGenres(
            List<Integer> genreIds, boolean matchAll, Pageable pageable) {

        if (genreIds == null || genreIds.isEmpty())
            throw new IllegalArgumentException("At least one genre id is required");

        List<Integer> songIds;

        if (matchAll) {
            songIds = songGenreRepository.findSongIdsByAllGenres(genreIds, genreIds.size());
        } else {
            songIds = songGenreRepository.findSongIdsByAnyGenre(genreIds);
        }

        if (songIds.isEmpty())
            return Page.empty(pageable);

        return songRepository.findByIdInAndStatus(
                        songIds, enumeration.SongStatus.LIVE, pageable)
                .map(songMapper::toSummaryResponse);
    }

    public void addGenre(Integer songId, Integer genreId,
                         Integer currentUserId, boolean isAdmin) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found: " + songId));

        if (!isAdmin && !song.getArtist().getId().equals(currentUserId))
            throw new AccessDeniedException("You can only tag genres on your own songs");

        if (songGenreRepository.existsBySongIdAndGenreId(songId, genreId))
            throw new DuplicateResourceException("Song already has this genre");

        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found: " + genreId));

        SongGenre sg = new SongGenre();
        sg.setSong(song);
        sg.setGenre(genre);
        songGenreRepository.save(sg);
    }

    public void removeGenre(Integer songId, Integer genreId,
                            Integer currentUserId, boolean isAdmin) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found: " + songId));

        if (!isAdmin && !song.getArtist().getId().equals(currentUserId))
            throw new AccessDeniedException("You can only remove genres from your own songs");

        if (!songGenreRepository.existsBySongIdAndGenreId(songId, genreId))
            throw new ResourceNotFoundException("Song does not have this genre");

        songGenreRepository.deleteBySongIdAndGenreId(songId, genreId);
    }
}