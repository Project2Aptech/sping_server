package org.example.spring_server.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.spring_server.enums.enumeration;

import java.time.LocalDateTime;

@Entity
@Table(name = "play_history")
@Getter @Setter @NoArgsConstructor
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @Column(name = "played_at", nullable = false)
    private LocalDateTime playedAt = LocalDateTime.now();

    @Column(name = "seconds_played", nullable = false)
    private Integer secondsPlayed = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type")
    private enumeration.DeviceType deviceType = enumeration.DeviceType.DESKTOP;
}