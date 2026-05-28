package org.example.spring_server.mapper;

import jakarta.persistence.*;
import org.example.spring_server.dto.AlbumDTO;
import org.example.spring_server.dto.HistoryDTO;
import org.example.spring_server.dto.SongDTO;
import org.example.spring_server.dto.UserDTO;
import org.example.spring_server.entity.Album;
import org.example.spring_server.entity.History;
import org.example.spring_server.entity.Song;
import org.example.spring_server.entity.User;
import org.example.spring_server.enums.enumeration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface HistoryMapper {

    HistoryDTO.HistoryResponse toResponse(History history);

    UserDTO.UserSummary toUserSummary(User user);

    SongDTO.SongSummaryResponse toSongSummary(Song song);
}
