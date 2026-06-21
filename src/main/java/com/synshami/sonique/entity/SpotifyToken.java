package com.synshami.sonique.entity;

import com.synshami.sonique.enums.SpotifyConnectionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "spotify_tokens",
        uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpotifyToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, unique = true)
    private String spotifyUserId;

    @Column(nullable = false, length = 1000)
    private String accessToken;

    @Column(nullable = false, length = 1000)
    private String refreshToken;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpotifyConnectionStatus connectionStatus;
}