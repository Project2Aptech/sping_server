package org.example.spring_server.mapper;

import org.example.spring_server.dto.GenreDTO;
import org.example.spring_server.entity.Genre;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GenreMapper {

    GenreDTO.GenreResponse toResponse(Genre genre);

    @Mapping(target = "id", ignore = true)
    Genre toEntity(GenreDTO.GenreRequest request);
}