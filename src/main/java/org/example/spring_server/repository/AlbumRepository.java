package org.example.spring_server.repository;

import org.example.spring_server.entity.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<Album, Integer> {
    Page<Album> findByArtistId(Integer artistId, Pageable pageable);
    boolean existsByArtistIdAndTitle(Integer artistId, String title);
    Page<Album> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}