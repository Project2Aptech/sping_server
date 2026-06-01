package org.example.spring_server.service;

import lombok.RequiredArgsConstructor;
import org.example.spring_server.dto.GenreDTO;
import org.example.spring_server.dto.SongDTO;
import org.example.spring_server.entity.Album;
import org.example.spring_server.entity.History;
import org.example.spring_server.entity.Song;
import org.example.spring_server.entity.User;
import org.example.spring_server.enums.enumeration;
import org.example.spring_server.exception.ResourceNotFoundException;
import org.example.spring_server.mapper.SongMapper;
import org.example.spring_server.repository.*;
import org.example.spring_server.service.cloudinary.CloudinaryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SongService {

    private final SongRepository songRepository;
    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;
    private final SongMapper songMapper;
    private final CloudinaryService cloudinaryService;
    private final HistoryRepository historyRepository;
    private final ArtistEarningService artistEarningService;
    private final SongGenreRepository songGenreRepository;

    @Transactional(readOnly = true)
    public SongDTO.SongDetailResponse findById(Integer id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found: " + id));
        return toDetailResponse(song);
    }

    @Transactional(readOnly = true)
    public Page<SongDTO.SongSummaryResponse> findByAlbum(Integer albumId, Pageable pageable) {
        return songRepository.findByAlbumId(albumId, pageable)
                .map(songMapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public Page<SongDTO.SongSummaryResponse> findByArtist(Integer artistId, Pageable pageable) {
        return songRepository.findByArtistId(artistId, pageable)
                .map(songMapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public Page<SongDTO.SongSummaryResponse> findLive(Pageable pageable) {
        return songRepository.findByStatus(enumeration.SongStatus.LIVE, pageable)
                .map(songMapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public Page<SongDTO.SongSummaryResponse> findByTitle(String title, Pageable pageable) {
        return songRepository.findByTitleContainingIgnoreCase(title, pageable)
                .map(songMapper::toSummaryResponse);
    }

    public SongDTO.SongDetailResponse create(SongDTO.SongRequest request, MultipartFile file,
                                             Integer currentUserId) throws IOException {

        // admin can create for any artist, artist can only create for themselves
        if (!currentUserId.equals(request.artistId()))
            throw new AccessDeniedException("You can only upload songs for your own account");

        // verify the artistId belongs to a user with ARTIST or ADMIN role
        if (!userRepository.existsByIdAndRole(request.artistId(), enumeration.UserRole.ARTIST)
                && !userRepository.existsByIdAndRole(request.artistId(), enumeration.UserRole.ADMIN))
            throw new AccessDeniedException("Target user is not an artist");

        User artist = userRepository.findById(request.artistId())
                .orElseThrow(() -> new ResourceNotFoundException("Artist not found: " + request.artistId()));

        String fileUrl = cloudinaryService.uploadAudio(file);

        Song song = songMapper.toEntity(request);
        song.setArtist(artist);
        song.setFileUrl(fileUrl);
        song.setStatus(enumeration.SongStatus.PENDING);

        if (request.albumId() != null) {
            Album album = albumRepository.findById(request.albumId())
                    .orElseThrow(() -> new ResourceNotFoundException("Album not found: " + request.albumId()));

            // verify album belongs to this artist
            if (!album.getArtist().getId().equals(request.artistId()))
                throw new AccessDeniedException("This album does not belong to you");

            song.setAlbum(album);
        }

        return toDetailResponse(songRepository.save(song));
    }

    public SongDTO.SongDetailResponse publish(Integer id, Integer currentUserId) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found: " + id));

        // null means admin — skip ownership check
        if (currentUserId != null && !song.getArtist().getId().equals(currentUserId))
            throw new AccessDeniedException("You can only publish your own songs");

        song.setStatus(enumeration.SongStatus.LIVE);
        return toDetailResponse(songRepository.save(song));
    }

    @Transactional
    public SongDTO.SongDetailResponse playback(Integer songId, Integer userId) {

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found: " + songId));

        if (song.getStatus() != enumeration.SongStatus.LIVE)
            throw new ResourceNotFoundException("Song not found: " + songId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (!user.getAccountType().canAccess(song.getRequiredAccountType()))
            throw new AccessDeniedException("Upgrade your account to play this song");

        song.setPlayCount(song.getPlayCount() + 1);

        artistEarningService.recordStream(song.getArtist().getId());

        return toDetailResponse(song);
    }

    public void delete(Integer id, Integer currentUserId) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found: " + id));

        if (currentUserId != null && !song.getArtist().getId().equals(currentUserId))
            throw new AccessDeniedException("You can only delete your own songs");

        song.setStatus(enumeration.SongStatus.DELETED);
        songRepository.save(song);
    }

    public void hardDelete(Integer id) {
        if (!songRepository.existsById(id))
            throw new ResourceNotFoundException("Song not found: " + id);
        songRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<SongDTO.SongSummaryResponse> findByGenre(Integer genreId, Pageable pageable) {
        return songRepository.findLiveByGenreId(genreId, pageable)
                .map(songMapper::toSummaryResponse);
    }
    public SongDTO.SongDetailResponse uploadCover(Integer songId, MultipartFile file,
                                                  Integer currentUserId, boolean isAdmin) throws IOException {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found: " + songId));

        if (!isAdmin && !song.getArtist().getId().equals(currentUserId))
            throw new AccessDeniedException("You can only update your own songs");

        String coverUrl = cloudinaryService.uploadImage(file);
        song.setCoverUrl(coverUrl);

        return toDetailResponse(songRepository.save(song));
    }

    private SongDTO.SongDetailResponse toDetailResponse(Song song) {
        List<GenreDTO.GenreSummaryResponse> genres = songGenreRepository
                .findBySongId(song.getId())
                .stream()
                .map(sg -> new GenreDTO.GenreSummaryResponse(
                        sg.getGenre().getId(),
                        sg.getGenre().getName(),
                        sg.getGenre().getSlug()
                ))
                .toList();

        return new SongDTO.SongDetailResponse(
                song.getId(),
                song.getArtist().getId(),
                song.getAlbum() != null ? song.getAlbum().getId() : null,
                song.getTitle(),
                song.getDurationSeconds(),
                song.getFileUrl(),
                song.getCoverUrl(),
                song.getTrackNumber(),
                song.getPlayCount(),
                song.getStatus(),
                song.getRequiredAccountType(),
                genres,
                song.getCreatedAt(),
                song.getUpdatedAt()
        );
    }
}