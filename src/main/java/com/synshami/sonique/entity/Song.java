package com.synshami.sonique.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "songs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String spotifyId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String artistName;

    private String albumName;

    private LocalDate releaseDate;

    private Integer durationMs;

    private Integer popularity;

    private Double energy;
}