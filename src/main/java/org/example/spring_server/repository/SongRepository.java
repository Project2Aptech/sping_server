package org.example.spring_server.repository;

import org.example.spring_server.entity.Song;
import org.example.spring_server.enums.enumeration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SongRepository extends JpaRepository<Song, Integer> {
    Page<Song> findByAlbumId(Integer albumId, Pageable pageable);
    Page<Song> findByArtistId(Integer artistId, Pageable pageable);
    Page<Song> findByStatus(enumeration.SongStatus status, Pageable pageable);
    Page<Song> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Song> findByIdInAndStatus(List<Integer> ids, enumeration.SongStatus status, Pageable pageable);
    @Query(
            value = "SELECT s FROM Song s LEFT JOIN FETCH s.artist LEFT JOIN FETCH s.album",
            countQuery = "SELECT COUNT(s) FROM Song s"
    )
    Page<Song> findAllWithDetails(Pageable pageable);
}