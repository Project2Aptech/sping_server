package org.example.spring_server.service;

import lombok.RequiredArgsConstructor;
import org.example.spring_server.dto.ArtistEarningDTO;
import org.example.spring_server.entity.ArtistEarning;
import org.example.spring_server.entity.User;
import org.example.spring_server.exception.ResourceNotFoundException;
import org.example.spring_server.repository.ArtistEarningRepository;
import org.example.spring_server.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class ArtistEarningService {

    private final ArtistEarningRepository artistEarningRepository;
    private final UserRepository userRepository;
    private static final BigDecimal RATE_PER_STREAM = new BigDecimal("0.004");

    @Transactional(readOnly = true)
    public Page<ArtistEarningDTO.ArtistEarningResponse> findByArtist(
            Integer artistId, Pageable pageable) {
        return artistEarningRepository.findByArtistIdOrderByPeriodStartDesc(artistId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ArtistEarningDTO.ArtistEarningSummary getSummary(Integer artistId) {
        BigDecimal total = artistEarningRepository.sumAmountByArtistId(artistId);
        Integer streams  = artistEarningRepository.sumStreamsByArtistId(artistId);
        return new ArtistEarningDTO.ArtistEarningSummary(total, streams);
    }

    public void recordStream(Integer artistId) {
        LocalDate periodStart = LocalDate.now().withDayOfMonth(1);
        LocalDate periodEnd   = periodStart.plusMonths(1).minusDays(1);

        User artist = userRepository.findById(artistId)
                .orElseThrow(() -> new ResourceNotFoundException("Artist not found: " + artistId));

        ArtistEarning earning = artistEarningRepository
                .findByArtistIdAndPeriodStart(artistId, periodStart)
                .orElseGet(() -> {
                    ArtistEarning e = new ArtistEarning();
                    e.setArtist(artist);
                    e.setPeriodStart(periodStart);
                    e.setPeriodEnd(periodEnd);
                    return e;
                });

        earning.setStreamCount(earning.getStreamCount() + 1);
        earning.setAmount(earning.getAmount().add(RATE_PER_STREAM));
        artistEarningRepository.save(earning);
    }

    private ArtistEarningDTO.ArtistEarningResponse toResponse(ArtistEarning e) {
        return new ArtistEarningDTO.ArtistEarningResponse(
                e.getId(),
                e.getArtist().getId(),
                e.getPeriodStart(),
                e.getPeriodEnd(),
                e.getStreamCount(),
                e.getAmount(),
                e.getCreatedAt()
        );
    }
}