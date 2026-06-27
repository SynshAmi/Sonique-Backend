package com.synshami.sonique.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    private String musicBrainzId;

    private Long listenerCount;

    private Long playCount;

    @Builder.Default
    @OneToMany(mappedBy = "artist")
    private List<ArtistTag> artistTags = new ArrayList<>();

    private LocalDateTime lastUpdated;
}