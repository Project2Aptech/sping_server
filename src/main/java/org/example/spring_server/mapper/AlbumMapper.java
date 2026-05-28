package org.example.spring_server.mapper;

import org.example.spring_server.dto.AlbumDTO;
import org.example.spring_server.entity.Album;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AlbumMapper {

    @Mapping(source = "artist.id",          target = "artistId")
    @Mapping(source = "artist.displayName", target = "artistName")
    AlbumDTO.AlbumResponse toResponse(Album album);

    AlbumDTO.AlbumSummaryResponse toSummaryResponse(Album album);

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "artist",    ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Album toEntity(AlbumDTO.AlbumRequest request);
}