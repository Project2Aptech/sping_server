package org.example.spring_server.repository;

import org.example.spring_server.entity.Song;
import org.example.spring_server.enums.enumeration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SongRepository extends JpaRepository<Song, Integer> {
    Page<Song> findByAlbumId(Integer albumId, Pageable pageable);
    Page<Song> findByArtistId(Integer artistId, Pageable pageable);
    Page<Song> findByStatus(enumeration.SongStatus status, Pageable pageable);
    Page<Song> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query("SELECT s FROM Song s JOIN SongGenre sg ON sg.song.id = s.id WHERE sg.genre.id = :genreId AND s.status = 'LIVE'")
    Page<Song> findLiveByGenreId(@Param("genreId") Integer genreId, Pageable pageable);
}