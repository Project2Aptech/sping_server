package org.example.spring_server.repository;

import org.example.spring_server.entity.SongGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SongGenreRepository extends JpaRepository<SongGenre, SongGenre.SongGenreId> {
    boolean existsBySongIdAndGenreId(Integer songId, Integer genreId);
    void deleteBySongIdAndGenreId(Integer songId, Integer genreId);
    List<SongGenre> findBySongId(Integer songId);

    @Query("SELECT sg.song.id FROM SongGenre sg WHERE sg.genre.id IN :genreIds GROUP BY sg.song.id HAVING COUNT(DISTINCT sg.genre.id) = :genreCount")
    List<Integer> findSongIdsByAllGenres(@Param("genreIds") List<Integer> genreIds, @Param("genreCount") long genreCount);
    @Query("SELECT DISTINCT sg.song.id FROM SongGenre sg WHERE sg.genre.id IN :genreIds")
    List<Integer> findSongIdsByAnyGenre(@Param("genreIds") List<Integer> genreIds);
}