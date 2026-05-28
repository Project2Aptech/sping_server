package org.example.spring_server.repository;

import org.example.spring_server.entity.ArtistEarning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface ArtistEarningRepository extends JpaRepository<ArtistEarning, Integer> {
    Page<ArtistEarning> findByArtistIdOrderByPeriodStartDesc(Integer artistId, Pageable pageable);

    Optional<ArtistEarning> findByArtistIdAndPeriodStart(Integer artistId, LocalDate periodStart);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM ArtistEarning e WHERE e.artist.id = :artistId")
    BigDecimal sumAmountByArtistId(@Param("artistId") Integer artistId);

    @Query("SELECT COALESCE(SUM(e.streamCount), 0) FROM ArtistEarning e WHERE e.artist.id = :artistId")
    Integer sumStreamsByArtistId(@Param("artistId") Integer artistId);
}