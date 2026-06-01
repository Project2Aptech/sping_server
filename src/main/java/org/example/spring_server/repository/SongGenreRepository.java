package org.example.spring_server.repository;

import org.example.spring_server.entity.SongGenre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SongGenreRepository extends JpaRepository<SongGenre, SongGenre.SongGenreId> {
    boolean existsBySongIdAndGenreId(Integer songId, Integer genreId);
    void deleteBySongIdAndGenreId(Integer songId, Integer genreId);
    List<SongGenre> findBySongId(Integer songId);

}