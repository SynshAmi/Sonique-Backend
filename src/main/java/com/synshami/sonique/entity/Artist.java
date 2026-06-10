package com.synshami.sonique.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "artists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String spotifyId;

    @Column(nullable = false)
    private String name;

    @ElementCollection
    @CollectionTable(
            name = "artist_raw_genres",
            joinColumns = @JoinColumn(name = "artist_id")
    )
    @Column(name = "genre")
    private Set<String> rawGenres;

    private Integer popularity;

    private LocalDateTime lastUpdated;
}