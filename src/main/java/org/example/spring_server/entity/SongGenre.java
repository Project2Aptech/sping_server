package org.example.spring_server.entity;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
@Entity
@Table(name = "song_genres")
@Getter @Setter @NoArgsConstructor
public class SongGenre {

    @EmbeddedId
    private SongGenreId id = new SongGenreId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("songId")
    @JoinColumn(name = "song_id")
    private Song song;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("genreId")
    @JoinColumn(name = "genre_id")
    private Genre genre;

    @Embeddable
    @Getter @Setter @NoArgsConstructor
    @EqualsAndHashCode
    public static class SongGenreId implements Serializable {
        @Column(name = "song_id")
        private Integer songId;

        @Column(name = "genre_id")
        private Integer genreId;
    }
}