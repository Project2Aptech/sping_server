package org.example.spring_server.repository;

import org.example.spring_server.entity.PlaylistSong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, PlaylistSong.PlaylistSongId> {
    Page<PlaylistSong> findByPlaylistId(Integer playlistId, Pageable pageable);
    boolean existsByPlaylistIdAndSongId(Integer playlistId, Integer songId);
    void deleteByPlaylistIdAndSongId(Integer playlistId, Integer songId);
}