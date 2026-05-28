package org.example.spring_server.mapper;

import org.example.spring_server.dto.SongDTO;
import org.example.spring_server.entity.Song;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SongMapper {

    @Mapping(source = "artist.id", target = "artistId")
    @Mapping(source = "album.id",  target = "albumId")
    SongDTO.SongDetailResponse toDetailResponse(Song song);

    @Mapping(source = "artist.id", target = "artistId")
    SongDTO.SongSummaryResponse toSummaryResponse(Song song);

    @Mapping(target = "id",                  ignore = true)
    @Mapping(target = "artist",              ignore = true)
    @Mapping(target = "album",               ignore = true)
    @Mapping(target = "fileUrl",             ignore = true)
    @Mapping(target = "playCount",           ignore = true)
    @Mapping(target = "status",              ignore = true)
    @Mapping(target = "createdAt",           ignore = true)
    @Mapping(target = "updatedAt",           ignore = true)
    Song toEntity(SongDTO.SongRequest request);
}