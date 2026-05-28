package org.example.spring_server.service;

import lombok.RequiredArgsConstructor;
import org.example.spring_server.dto.SongDTO;
import org.example.spring_server.entity.Album;
import org.example.spring_server.entity.History;
import org.example.spring_server.entity.Song;
import org.example.spring_server.entity.User;
import org.example.spring_server.enums.enumeration;
import org.example.spring_server.exception.ResourceNotFoundException;
import org.example.spring_server.mapper.SongMapper;
import org.example.spring_server.repository.AlbumRepository;
import org.example.spring_server.repository.HistoryRepository;
import org.example.spring_server.repository.SongRepository;
import org.example.spring_server.repository.UserRepository;
import org.example.spring_server.service.cloudinary.CloudinaryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

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

    @Transactional(readOnly = true)
    public SongDTO.SongDetailResponse findById(Integer id) {
        return songRepository.findById(id)
                .map(songMapper::toDetailResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found: " + id));
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

        if (!request.artistId().equals(currentUserId))
            throw new AccessDeniedException("You can only upload songs for your own account");

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
            song.setAlbum(album);
        }

        return songMapper.toDetailResponse(songRepository.save(song));
    }

    public SongDTO.SongDetailResponse publish(Integer id, Integer currentUserId) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found: " + id));

        // null means admin — skip ownership check
        if (currentUserId != null && !song.getArtist().getId().equals(currentUserId))
            throw new AccessDeniedException("You can only publish your own songs");

        song.setStatus(enumeration.SongStatus.LIVE);
        return songMapper.toDetailResponse(songRepository.save(song));
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

        return songMapper.toDetailResponse(song);
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
}