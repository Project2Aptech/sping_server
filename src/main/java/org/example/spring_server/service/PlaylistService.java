package org.example.spring_server.service;

import lombok.RequiredArgsConstructor;
import org.example.spring_server.dto.PlaylistDTO;
import org.example.spring_server.dto.SongDTO;
import org.example.spring_server.entity.*;
import org.example.spring_server.exception.DuplicateResourceException;
import org.example.spring_server.exception.ResourceNotFoundException;
import org.example.spring_server.mapper.SongMapper;
import org.example.spring_server.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final PlaylistFollowRepository playlistFollowRepository;
    private final SongRepository songRepository;
    private final UserRepository userRepository;
    private final SongMapper songMapper;

    @Transactional(readOnly = true)
    public Page<PlaylistDTO.PlaylistSummaryResponse> findAll(Pageable pageable) {
        return playlistRepository.findAll(pageable)
                .map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public Page<PlaylistDTO.PlaylistSummaryResponse> findPublic(Pageable pageable) {
        return playlistRepository.findByIsPublicTrue(pageable)
                .map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public Page<PlaylistDTO.PlaylistSummaryResponse> findByUser(Integer userId, Pageable pageable) {
        return playlistRepository.findByUserId(userId, pageable)
                .map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public PlaylistDTO.PlaylistResponse findById(Integer id) {
        return playlistRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<SongDTO.SongSummaryResponse> findSongs(Integer playlistId,
                                                       Integer currentUserId, boolean isAdmin, Pageable pageable) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found: " + playlistId));

        if (!playlist.isPublic()
                && !isAdmin
                && (!playlist.getUser().getId().equals(currentUserId)))
            throw new AccessDeniedException("This playlist is private");

        return playlistSongRepository.findByPlaylistId(playlistId, pageable)
                .map(ps -> songMapper.toSummaryResponse(ps.getSong()));
    }

    public PlaylistDTO.PlaylistResponse create(Integer userId, PlaylistDTO.PlaylistRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Playlist playlist = new Playlist();
        playlist.setUser(user);
        playlist.setTitle(request.title());
        playlist.setDescription(request.description());
        playlist.setCoverUrl(request.coverUrl());
        playlist.setPublic(request.isPublic());

        return toResponse(playlistRepository.save(playlist));
    }

    public PlaylistDTO.PlaylistResponse update(Integer id, PlaylistDTO.PlaylistUpdateRequest request,
                                               Integer currentUserId, boolean isAdmin) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found: " + id));

        if (!isAdmin && !playlist.getUser().getId().equals(currentUserId))
            throw new AccessDeniedException("You can only update your own playlists");

        if (request.title() != null)       playlist.setTitle(request.title());
        if (request.description() != null) playlist.setDescription(request.description());
        if (request.coverUrl() != null)    playlist.setCoverUrl(request.coverUrl());
        if (request.isPublic() != null)    playlist.setPublic(request.isPublic());

        return toResponse(playlistRepository.save(playlist));
    }

    public void delete(Integer id, Integer currentUserId, boolean isAdmin) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found: " + id));

        if (!isAdmin && !playlist.getUser().getId().equals(currentUserId))
            throw new AccessDeniedException("You can only delete your own playlists");

        playlistRepository.deleteById(id);
    }

    public void addSong(Integer playlistId, Integer songId, Integer currentUserId, boolean isAdmin) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found: " + playlistId));

        if (!isAdmin && !playlist.getUser().getId().equals(currentUserId))
            throw new AccessDeniedException("You can only add songs to your own playlists");

        if (playlistSongRepository.existsByPlaylistIdAndSongId(playlistId, songId))
            throw new DuplicateResourceException("Song already in playlist");

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found: " + songId));

        PlaylistSong ps = new PlaylistSong();
        ps.setPlaylist(playlist);
        ps.setSong(song);
        playlistSongRepository.save(ps);
    }

    public void removeSong(Integer playlistId, Integer songId, Integer currentUserId, boolean isAdmin) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found: " + playlistId));

        if (!isAdmin && !playlist.getUser().getId().equals(currentUserId))
            throw new AccessDeniedException("You can only remove songs from your own playlists");

        if (!playlistSongRepository.existsByPlaylistIdAndSongId(playlistId, songId))
            throw new ResourceNotFoundException("Song not in playlist");

        playlistSongRepository.deleteByPlaylistIdAndSongId(playlistId, songId);
    }

    public void follow(Integer userId, Integer playlistId) {
        if (playlistFollowRepository.existsByUserIdAndPlaylistId(userId, playlistId))
            throw new DuplicateResourceException("Already following this playlist");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found: " + playlistId));

        PlaylistFollow follow = new PlaylistFollow();
        follow.setUser(user);
        follow.setPlaylist(playlist);
        playlistFollowRepository.save(follow);
    }

    public void unfollow(Integer userId, Integer playlistId) {
        if (!playlistFollowRepository.existsByUserIdAndPlaylistId(userId, playlistId))
            throw new ResourceNotFoundException("Not following this playlist");
        playlistFollowRepository.deleteByUserIdAndPlaylistId(userId, playlistId);
    }

    private PlaylistDTO.PlaylistResponse toResponse(Playlist p) {
        return new PlaylistDTO.PlaylistResponse(
                p.getId(),
                p.getUser().getId(),
                p.getTitle(),
                p.getDescription(),
                p.getCoverUrl(),
                p.isPublic(),
                p.getCreatedAt()
        );
    }

    private PlaylistDTO.PlaylistSummaryResponse toSummary(Playlist p) {
        return new PlaylistDTO.PlaylistSummaryResponse(
                p.getId(),
                p.getTitle(),
                p.getCoverUrl(),
                p.isPublic()
        );
    }
}